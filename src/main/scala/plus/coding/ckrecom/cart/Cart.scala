package plus.coding.ckrecom
package cart

import plus.coding.ckrecom.Tax.TaxClass
import javax.money.CurrencyUnit
import java.math.{ BigDecimal, MathContext }
import scala.collection.immutable.Seq
import scala.util.{ Try, Success, Failure }

object Cart {

  import plus.coding.ckrecom.Implicits.mathContext

  /** Checks if the cart contains failed price calculations.
    *
    * Returns a list of errors (or an empty list if the cart is valid).
    */
  def validate(cart: Cart): Seq[Throwable] = {
    cart.contents.foldRight(Seq.empty[Throwable]) {
      case (CartContentItem(_, Success(_)), errs) => errs
      case (CartContentItem(_, Failure(e)), errs) => e +: errs
    }
  }

  /** Builds a cart from a sequence of not-yet-calculated items, currency and tax price mode.
    */
  def apply[T <: CartItemPre[_]](cur: CurrencyUnit, mode: PriceMode.Value, preItems: Seq[T])(implicit mc: MathContext): Cart = {
    val cart = new Cart(Seq.empty, cur, mode)(mc)
    (cart /: preItems) {
      case (c: Cart, item: CartItemPre[_]) => {
        val prices = item.finalPrices(c)
        c.addContent(CartContentItem(item.priceable, prices))
      }
    }
  }

}

/** The main cart class. Represents a cart with already calculated prices.
  *
  */
case class Cart(
    val contents: Seq[CartContentItem],
    val currency: CurrencyUnit,
    val mode: PriceMode.Value)(implicit val mc: MathContext) {

  // mainly for debugging, as we don't have
  // "real" renderers yet.
  // TODO
  //override def toString = {
  //}

  /** Adds an item to the cart.
    *
    * This does not validate the content. You should call
    * validate the final cart instance whenever you
    * update a cart.
    */
  def addContent(item: CartContentItem): Cart = {
    new Cart(contents :+ item, currency, mode)(mc)
  }

  /** Calculates the grand total of the current cart state.
    *
    * Only already calculated contents are summed up.
    */
  def grandTotal(): BigDecimal = {
    def sum(xs: Seq[BigDecimal]): BigDecimal = {
      xs.foldLeft(BigDecimal.ZERO)(_.add(_, mc))
    }
    val itemSums = for {
      item <- contents
      prices = item.finalPrices match {
        case Failure(_)  => Seq.empty
        case Success(ps) => ps
      }
      priceAmnts = prices.map(_._1)
    } yield sum(priceAmnts)
    sum(itemSums)
  }

  def taxes(): BigDecimal = ???

  def valueByTaxClass: Map[TaxClass, (TaxRate, BigDecimal)] = ???
}
