package plus.coding.ckrecom
package cart

import javax.money.CurrencyUnit
import java.math.{ BigDecimal, MathContext }
import scala.collection.immutable.Seq
import scala.util.{ Try, Success, Failure }
import plus.coding.ckrecom.tax.{ TaxRate, TaxSystem }
import plus.coding.ckrecom.tax.TaxSystem

object Cart {

  /** Builds a cart from a sequence of not-yet-calculated items (`CartItemPre` instances),
    * currency and tax price mode.
    */
  def apply[T <: CartItemPre[_, U], U: TaxSystem](preItems: Seq[T], cur: CurrencyUnit, mode: PriceMode.Value)(implicit mc: MathContext): Cart[U] = {
    val cart = new Cart(cur, mode, Seq.empty)
    (cart /: preItems) {
      case (c: Cart[U], item: CartItemPre[_, _]) => {
        val prices = item.finalPrices(c)
        c.addContent(CartContentItem(item.priceable, prices))
      }
    }
  }

}

abstract class CartBase[T: TaxSystem] {

  implicit val mc: MathContext

  case class Contents[T](cs: Seq[CartContentItem[T]])

  val contents: Seq[CartContentItem[T]] = Seq.empty

  val currency: CurrencyUnit

  val mode: PriceMode.Value

  /** Checks if the cart contains failed price calculations.
    *
    * Returns a list of errors (or an empty list if the cart is valid).
    */
  def validate: Seq[Throwable] = {
    contents.foldRight(Seq.empty[Throwable]) {
      case (CartContentItem(_, Success(_)), errs) => errs
      case (CartContentItem(_, Failure(e)), errs) => e +: errs
    }
  }

  /** Calculates the grand total of the cart.
    */
  def grandTotal(): Long = {
    val itemSums = for {
      item <- contents
      prices = item.results match {
        case Failure(_)  => Seq.empty
        case Success(ps) => ps
      }
      priceAmnts = prices.map(_.price)
    } yield priceAmnts.sum
    itemSums.sum
  }

  // TODO calculate taxes
  def taxes(): BigDecimal = ???

  // TODO calculate amount per tax class
  def valueByTaxClass: Map[T, (TaxRate, BigDecimal)] = ???
}

/** The main cart class. Represents a cart with already calculated prices.
  *
  */
case class Cart[T: TaxSystem](
  override val currency: CurrencyUnit,
  override val mode: PriceMode.Value,
  override val contents: Seq[CartContentItem[T]] = Seq.empty)(implicit val mc: MathContext)
    extends CartBase[T] {

  /** Adds an item to the cart.
    *
    * This does not validate the content. You should call
    * validate the final cart instance whenever you
    * update a cart.
    */
  def addContent(item: CartContentItem[T]): Cart[T] = {
    copy(contents = contents ++ Seq(item))
  }

  def debugString: String = {
    val title = "Debugging cart:\n===============\n"
    val cur = "Currency: " ++ currency.toString() + '\n'
    val pMode = "Price mode: " ++ mode.toString() + '\n'
    val contentStr = ("contents:\n---------\n" /: contents) {
      case (acc, item) => acc ++ (" - " ++ item.toString + '\n')
    }
    title ++ cur ++ pMode ++ contentStr // TODO use some concatenation / sequencing function instead...
  }
}
