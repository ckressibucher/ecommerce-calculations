package plus.coding.ckrecom.cart

import scala.collection.immutable._
import scala.util.Try
import plus.coding.ckrecom.tax.TaxSystem
import plus.coding.ckrecom.tax.TaxSystem

/** A priceable together with the definition how to calculate final prices.
  *
  * A list of those "pre cart items" together with a cart definition should be
  * enough to calculate the cart.
  */
abstract class CartItemPre[P <: Priceable[T], T: TaxSystem] {

  /** Returns the wrapped priceable
    */
  def priceable: P

  /** Calculates final prices for this item
    */
  def finalPrices(cart: Cart[T]): PriceResult[T]
}
