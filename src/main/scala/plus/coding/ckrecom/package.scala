package plus.coding

import scala.collection.immutable._

package object ckrecom {

  object PriceMode extends Enumeration {
    val PRICE_NET, PRICE_GROSS = Value
  }

  type TaxTotals[T] = Map[T, Long]

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
  type PriceResult[T] = Either[String, TaxTotals[T]]

  /** @tparam P The "priceable" thing that should be put into the cart
    * @tparam T The tax class type
    */
  case class CartContentItem[P, T](priceable: P, results: PriceResult[T], isMainItem: Boolean)

  type CartResult[T] = Either[Seq[String], CartBase[T]]

}
