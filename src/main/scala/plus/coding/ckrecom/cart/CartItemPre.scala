package plus.coding.ckrecom.cart

import scala.collection.immutable._
import scala.util.Try

/**
 * A function which is responsible to calculate the final prices for one cart "pricable" (of type `T`).
 */
abstract class ItemCalc[T] extends ((Cart, T) => Try[Seq[TaxedPrice]])

/**
 * A priceable together with the definition how to calculate final prices.
 *
 * A list of those "pre cart items" together with a cart definition should be
 * enough to calculate the cart.
 */
case class CartItemPre[T <: Priceable](p: T, calc: ItemCalc[T])
