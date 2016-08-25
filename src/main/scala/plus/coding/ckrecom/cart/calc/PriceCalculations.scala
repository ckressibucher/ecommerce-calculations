package plus.coding.ckrecom.cart.calc

import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.tax.TaxSystem
import Priceable._
import scala.util.{ Success }
import scala.collection.immutable.Seq
import java.math.BigDecimal
import scala.util.Success
import plus.coding.ckrecom.tax.TaxRate
import java.math.MathContext

/** A helper for price calculations.
  */
trait PriceCalculations {

  /** Analyzes the final prices of all `Line` items, and builds a map TaxClass -> Summed amounts */
  def linePricesByTaxClass[T: TaxSystem](cart: CartBase[T]): Map[T, Long] = {
    val lnPrices = linePrices(cart)
    pricesByTaxClass(lnPrices, cart)
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
      case (acc, Success(prices)) =>
        prices.foldLeft(acc) {
          case (accLine, TaxedPrice(amnt, taxCls)) => {
            val current = accLine.getOrElse(taxCls, 0L)
            accLine.updated(taxCls, current + amnt)
          }
        }
      case (acc, _) => acc
    }
  }

  // TODO should we exclude free tax somehow?
  def cheapestTaxClass[T: TaxSystem](cart: CartBase[T])(implicit ord: Ordering[TaxRate]): Option[T] = {
    import ord.{ Ops, mkOrderingOps }

    val lnPrices = linePrices(cart)
    val taxSystem = implicitly[TaxSystem[T]]
    val ts: Seq[T] = (lnPrices.collect {
      case Success(prices) => prices map { _.taxClass }
    }).flatten

    val init: Option[T] = None
    (init /: ts) {
      case (None, tcls) => Some(tcls)
      case (acc @ Some(accCls), tcls) => {
        val rateAcc = taxSystem.rate(accCls)
        val rateNew = taxSystem.rate(tcls)
        if (rateAcc < rateNew) acc else (Some(tcls))
      }
    }
  }

  private def linePrices[T: TaxSystem](cart: CartBase[T]): Seq[PriceResult[T]] = {
    val lines: Seq[CartContentItem[T]] = cart.contents.filter {
      case CartContentItem(line, _) if line.isInstanceOf[Line[T]] => true
      case _ => false
    }
    lines map { _.results }
  }
}
