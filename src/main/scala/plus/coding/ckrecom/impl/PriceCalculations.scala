package plus.coding.ckrecom
package impl

import plus.coding.ckrecom.impl.Priceable._

import scala.collection.immutable.Seq
import java.math.{BigDecimal, RoundingMode}

/** A helper for price calculations.
  */
trait PriceCalculations {

  /** Analyzes the final prices of all `Line` items, and builds a map TaxClass -> Summed amounts */
  def mainItemPricesByTaxClass[T: TaxSystem](cart: CartBase[T]): Map[T, Long] = {
    val mainPrices = mainItemPrices(cart)
    pricesByTaxClass(mainPrices, cart)
  }

  def allPricesByTaxClass[T: TaxSystem](cart: CartBase[T]): Map[T, Long] = {
    val priceResults = cart.contents map {
      _.results
    }
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

  /**
    * Splits the given price into parts, corresponding to the given `distributionKey`.
    * Uses delta rounding to produce integer values (of type `Long`)
    *
    * @param distributionKey A map of tax classes to a value. The values of all classes define
    *                        the distribution, their sum can be anything.
    *                        Negative values are filtered out of this map.
    *                        If the remaining map is empty, an empty map is returned.
    * @return An empty map (if the given `distributionMap` is empty or only contains zeros or negative values),
    *         or a map like the given distribution key -- without zero or negative values --, but the sum
    *         of all values are equal to the given `price`.
    */
  def distributeByTaxClass[T](price: Long, distributionKey: Map[T, Long]): Map[T, Long] = {
    val newDistMap = distributionKey.filter(_._2 > 0)
    val distSum = newDistMap.foldLeft(new BigDecimal(0L)) {
      case (acc, (_, v)) if v > 0 => acc.add(new BigDecimal(v))
    }
    // for simplicity, we use BigDecimals to do decimal calculations. Performance could
    // probably be improved by using Integer divisions and modulo operations.
    val priceBig = new BigDecimal(price)
    val distributed = newDistMap.foldLeft((Map[T, Long](), new BigDecimal(0))) {
      case ((accMap, delta), (taxCls, amnt)) =>
        val amntBig = new BigDecimal(amnt)
        val preResult = priceBig.multiply(amntBig).setScale(15).divide(distSum, RoundingMode.HALF_EVEN)
                            .add(delta)
        val rounded = preResult.setScale(0, RoundingMode.HALF_EVEN)
        (accMap.updated(taxCls, rounded.longValue()), preResult.subtract(rounded))
    }
    distributed._1
  }

  private def mainItemPrices[T: TaxSystem](cart: CartBase[T]): Seq[PriceResult[T]] = {
    val lines: Seq[CartContentItem[_, T]] = cart.contents.filter {
      case CartContentItem(_, _, isMainItem) => isMainItem
    }
    lines map {
      _.results
    }
  }
}
