package plus.coding.ckrecom

import scala.math.Numeric
import scala.collection.immutable._
import java.math.BigDecimal
import Tax.TaxClass

package object cart {

  sealed trait PRICE_MODE
  case object PRICE_NET extends PRICE_MODE
  case object PRICE_GROSS extends PRICE_MODE

  /** A function which updates a cart with some calculations */
  type CartCalculator = (Cart[_] => Cart[_])

  /** A price used in the cart.
    * 
    * The cart has a defined currency,
    * so we don't need one in the price.
    * But we need a TaxClass which should be
    * used to calculate the net/gross amount
    * (the given numeric value represents the
    * type defined for the cart's mode).
    *
    * Note that T is expected to be a numeric type,
    * so you may want to constrain it to have a
    * Numeric implementation whenever you use it.
    */
  type TaxedPrice = (BigDecimal, TaxClass)

  /** Used to define the contents of a cart.
    *
    * Can be something like a cart line of product and quantity,
    * a shipping fee, or a discount; togehter with a list of
    * calculated prices resulting from this thing.
    */
  type CartContentItem = (Priceable, Seq[TaxedPrice])

}
