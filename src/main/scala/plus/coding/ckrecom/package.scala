package plus.coding

import scala.collection.immutable._

package object ckrecom {

  object PriceMode extends Enumeration {
    val PRICE_NET, PRICE_GROSS = Value
  }

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

  /** @tparam P The "priceable" thing that should be put into the cart
    * @tparam T The tax class type
    */
  sealed trait CartContentItem[P, T] {
    val priceable: P
    val isMainItem: Boolean
  }

  case class FailedItem[P, T](priceable: P, error: String, isMainItem: Boolean)
    extends CartContentItem[P, T]

  case class SuccessItem[P, T](priceable: P, priceResults: Map[T, Long], isMainItem: Boolean)
    extends CartContentItem[P, T]

  type CartResult[T] = Either[InterimCart[T], SuccessCart[T]]

}
