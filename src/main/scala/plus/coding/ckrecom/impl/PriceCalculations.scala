package plus.coding.ckrecom
package impl

import plus.coding.ckrecom.impl.Priceable._

import scala.collection.immutable.Seq

/** A helper for price calculations.
  */
trait PriceCalculations {

  /** Analyzes the final prices of all `Line` items, and builds a map TaxClass -> Summed amounts */
  def linePricesByTaxClass[T: TaxSystem](cart: CartBase[T]): Map[T, Long] = {
    val mainPrices = mainItemPrices(cart)
    pricesByTaxClass(mainPrices, cart)
  }

  def allPricesByTaxClass[T: TaxSystem](cart: CartBase[T]): Map[T, Long] = {
    val priceResults = cart.contents map { _.results }
    pricesByTaxClass(priceResults, cart)
  }

  /** Sums up the (successful calculated) prices by tax class.
    */
  def pricesByTaxClass[T: TaxSystem](priceResults: Seq[PriceResult[T]], cart: CartBase[T]): Map[T, Long] = {
    val init = Map[T, Long]()
    (init /: priceResults) {
      case (acc, Right(prices)) =>
        prices.foldLeft(acc) {
          case (accLine, (taxCls, amnt)) =>
            val current = accLine.getOrElse(taxCls, 0L)
            accLine.updated(taxCls, current + amnt)
        }
      case (acc, _) => acc
    }
  }

  // TODO should we exclude free tax somehow?
  def cheapestTaxClass[T: TaxSystem](cart: CartBase[T])(implicit ord: Ordering[TaxRate]): Option[T] = {
    import ord.mkOrderingOps

    val mainPrices = mainItemPrices(cart)
    val taxSystem = implicitly[TaxSystem[T]]
    val ts: Seq[T] = mainPrices.collect {
      case Right(prices) => prices.keys
    }.flatten

    val init: Option[T] = None
    (init /: ts) {
      case (None, tcls) => Some(tcls)
      case (acc @ Some(accCls), tcls) =>
        val rateAcc = taxSystem.rate(accCls)
        val rateNew = taxSystem.rate(tcls)
        if (rateAcc < rateNew) acc else Some(tcls)
    }
  }

  private def mainItemPrices[T: TaxSystem](cart: CartBase[T]): Seq[PriceResult[T]] = {
    val lines: Seq[CartContentItem[_, T]] = cart.contents.filter {
      case CartContentItem(_, _, isMainItem) => isMainItem
    }
    lines map { _.results }
  }
}
