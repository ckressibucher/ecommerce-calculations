package plus.coding.ckrecom

import scala.collection.immutable._

/** A priceable together with the definition how to calculate final prices.
  *
  * A list of these `CartItemCalculator`s, together with a cart definition
  * should be enough to calculate the cart (see the `CartSystem` trait).
  * The resulting prices are later used as the
  * base for further calculations like discounts or taxes.
  *
  * @tparam P The "priceable" thing that should be put into the cart
  * @tparam T The tax class type
  */
abstract class CartItemCalculator[P, T] {

  /** Returns the wrapped priceable
    */
  def priceable: P

  /** Calculates final prices for this item
    */
  def finalPrices(cart: CartBase[T]): PriceResult[T]
}
