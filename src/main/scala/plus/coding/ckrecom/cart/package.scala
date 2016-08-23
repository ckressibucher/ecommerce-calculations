package plus.coding.ckrecom

import scala.math.Numeric
import scala.collection.immutable.Seq
import java.math.BigDecimal
import scala.util.Try
import plus.coding.ckrecom.tax.TaxSystem

package object cart {

  /** A price used in the cart.
    *
    * The cart has a defined currency,
    * so we don't need one in the price.
    * But we need a TaxClass which should be
    * used to calculate the net/gross amount
    * (the given numeric value represents the
    * type defined for the cart's mode).
    *
    */
  case class TaxedPrice[T: TaxSystem](price: BigDecimal, taxClass: T)

  /** The result of a price calculation, which uses
    * the `TaxClass` defined here.
    */
  type PriceResult[T] = Try[Seq[TaxedPrice[T]]]

  case class CartContentItem[T: TaxSystem](priceable: Priceable[T], results: PriceResult[T])
}
