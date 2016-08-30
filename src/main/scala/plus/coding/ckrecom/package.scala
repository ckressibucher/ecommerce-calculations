package plus.coding

import scala.collection.immutable._

package object ckrecom {
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
    */
  type PriceResult[T] = Either[String, Map[T, Long]]

  case class CartContentItem[P, T: TaxSystem](priceable: P, results: PriceResult[T])

  type TaxTotals[T] = Map[T, Long]

  type CartResult[T] = Either[Seq[String], CartBase[T]]

}
