package plus.coding.ckrecom

import scala.math.Numeric
import scala.collection.immutable.Seq
import java.math.BigDecimal
import scala.util.Try
import plus.coding.ckrecom.tax.TaxSystem

package object cart {

  class CalculationException(msg: String, prev: Throwable = null) extends RuntimeException(msg, prev)

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
  case class TaxedPrice[T: TaxSystem](price: Long, taxClass: T)

  /** The result of a price calculation, which uses
    * the `TaxClass` defined here.
    *
    * TODO maybe we could replace Seq[TaxedPrice] with Map[TaxClass, Long]
    */
  type PriceResult[T] = Try[Seq[TaxedPrice[T]]]

  case class CartContentItem[P, T: TaxSystem](priceable: P, results: PriceResult[T])

  type TaxTotals[T] = Map[T, Long]
}
