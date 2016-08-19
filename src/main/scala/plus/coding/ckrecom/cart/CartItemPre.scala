package plus.coding.ckrecom.cart

import scala.collection.immutable._
import scala.util.Try

/** A priceable together with the definition how to calculate final prices.
  *
  * A list of those "pre cart items" together with a cart definition should be
  * enough to calculate the cart.
  */
trait CartItemPre[T <: Priceable] {

  /** Returns the wrapped priceable
    */
  def priceable: T

  /** Calculates final prices for this item
    */
  def finalPrices(cart: Cart): Try[Seq[TaxedPrice]]
}
