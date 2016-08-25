package plus.coding.ckrecom
package cart

import javax.money.CurrencyUnit
import java.math.{ BigDecimal, MathContext }
import scala.collection.immutable.Seq
import scala.util.{ Try, Success, Failure }
import plus.coding.ckrecom.tax.{ TaxRate, TaxSystem }
import plus.coding.ckrecom.tax.TaxSystem
import plus.coding.ckrecom.cart.calc.PriceCalculations
import java.math.RoundingMode

object Cart {

  /** Builds a cart from a sequence of not-yet-calculated items (`CartItemPre` instances),
    * currency and tax price mode.
    *
    * (this is not named `apply` to avoid overloaded methods to help debugging)
    */
  def fromItems[T <: CartItemPre[_, U], U: TaxSystem](preItems: Seq[T], cur: CurrencyUnit, mode: PriceMode.Value)(implicit mc: MathContext): Cart[U] = {
    val cart = new Cart(cur, mode, Seq.empty)
    (cart /: preItems) {
      case (c: Cart[U], item: CartItemPre[_, _]) => {
        val prices = item.finalPrices(c)
        c.addContent(CartContentItem(item.priceable, prices))
      }
    }
  }

}

abstract class CartBase[T: TaxSystem] extends PriceCalculations {

  implicit val mc: MathContext
  val taxSystem = implicitly[TaxSystem[T]]

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

  def taxes(rounding: RoundingMode = RoundingMode.HALF_UP): TaxTotals[T] = {
    allPricesByTaxClass(this) map {
      case (taxCls, price) => {
        val rate = taxSystem.rate(taxCls)
        val newPrice = mode match {
          case PriceMode.PRICE_GROSS => rate.taxValueFromGross(price)
          case PriceMode.PRICE_NET   => rate.taxValue(price)
        }
        (taxCls, newPrice.setScale(0, rounding).longValue())
      }
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

  /** Returns a string describing the contents of this object.
    */
  def debugString: String = {
    val title = "Debugging cart:\n==============="
    val cur = "Currency: " ++ currency.toString()
    val pMode = "Price mode: " ++ mode.toString()
    val contentStr = ("contents:\n---------\n" /: contents) {
      case (acc, item) => acc ++ (" - " ++ item.toString + '\n')
    }
    val taxesStr = ("Taxes:\n------------\n" /: taxes()) {
      case (acc, (cls, amnt)) => acc ++ (cls.toString ++ " : " ++ amnt.toString() + '\n')
    }
    val totalSum = "Total: " ++ grandTotal().toString()
    List(title, cur, pMode, contentStr, taxesStr, totalSum).mkString("\n")
  }
}
