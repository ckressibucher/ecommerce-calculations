package plus.coding.ckrecom

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

  /** Whether this item is part of the "main items" of a cart.
    * This items are the main selling goods and can be used
    * to calculate interim sums, e.g. to use the ratio between
    * the sum of product prices of different tax classes.
    * @return
    */
  def isMainItem: Boolean

  /** Calculates final prices for this item
    */
  def finalPrices(cart: CartBase[T]): PriceResult[T]
}
