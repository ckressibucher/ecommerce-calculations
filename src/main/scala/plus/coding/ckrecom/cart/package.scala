package plus.coding.ckrecom

import scala.util.Try

import plus.coding.ckrecom.tax.TaxSystem

package object cart {

  class CalculationException(msg: String, prev: Throwable = null) extends RuntimeException(msg, prev)

  /** The result of a price calculation.
    *
    * This encodes:
    * - The tax class (or tax class hierarchy) used
    * - Success of failure of the calculation
    * - Calculated prices of type `Long`, each mapped to a specific tax class
    *
    * The price values are expected to be "final", i.e. they are expected to
    * represent a useful amount of money, and should thus be integral numbers
    * (representing the smallest unit of the currency).
    *
    * The currency is not encoded in this type. It's expected to use this values
    * only in conjunction with a cart (or something else) that defines the
    * currency for this prices.
    */
  type PriceResult[T] = Try[Map[T, Long]]

  case class CartContentItem[P, T: TaxSystem](priceable: P, results: PriceResult[T])

  type TaxTotals[T] = Map[T, Long]

  type CartResult[T] = Either[Seq[Throwable], CartBase[T]]
}
