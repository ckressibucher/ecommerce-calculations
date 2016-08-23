package plus.coding.ckrecom.cart.calc

import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.tax.TaxSystem
import Priceable._
import scala.util.{ Success }
import scala.collection.immutable.Seq
import java.math.BigDecimal
import plus.coding.ckrecom.tax.TaxSystem
import scala.util.Success

/** A helper for price calculations.
  */
trait PriceCalculations {

  /** Analyzes the final prices of all `Line` items, and builds a map TaxClass -> Summed amounts */
  def pricesByTaxClass[T: TaxSystem](cart: Cart[T]): Map[T, BigDecimal] = {
    val lnPrices = linePrices(cart)
    val init = Map[T, BigDecimal]()
    (init /: lnPrices) {
      case (acc, Success(prices)) =>
        prices.foldLeft(acc) {
          case (accLine, TaxedPrice(amnt, taxCls)) => {
            val newAmount = accLine.getOrElse(taxCls, BigDecimal.ZERO).add(amnt, cart.mc)
            accLine.updated(taxCls, newAmount)
          }
        }
      case (acc, _) => acc
    }
  }

  def cheapestTaxClass[T: TaxSystem](cart: Cart[T]): Option[T] = {
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
        if (rateAcc.compare(rateNew) < 0) acc else (Some(tcls))
      }
    }
  }

  private def linePrices[T: TaxSystem](cart: Cart[T]): Seq[PriceResult[T]] = {
    val lines: Seq[CartContentItem[T]] = cart.contents.filter {
      case CartContentItem(line, _) if line.isInstanceOf[Line[T]] => true
      case _ => false
    }
    lines map { _.results }
  }
}
