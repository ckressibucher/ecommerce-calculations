package plus.coding.ckrecom
package impl

import java.math.BigDecimal

import scala.collection.immutable._
import scala.util.Success

import plus.coding.ckrecom.impl.Priceable._

/** Applies a fixed discount, using the cheapest tax class of any of the products in cart (or the fallbackTaxClass when no
  * products are in the cart).
  */
class FixedDiscountCalc[T: TaxSystem](val priceable: FixedDiscount, fallbackTaxClass: T)
    extends CartItemCalculator[FixedDiscount, T] with PriceCalculations {

  def finalPrices(c: CartBase[T]): PriceResult[T] = {
    val taxClass = cheapestTaxClass(c).getOrElse(fallbackTaxClass)
    Success(Map(taxClass -> (-1 * priceable.amount)))
  }

}

// TODO Fixed discounts with distributed tax classes, or a fixed tax class

class PctDiscountCalc[T: TaxSystem](val priceable: PctDiscount)(implicit val rounding: Rounding)
    extends CartItemCalculator[PctDiscount, T] with PriceCalculations {

  def finalPrices(c: CartBase[T]): PriceResult[T] = {
    val productPricesByTaxClass = linePricesByTaxClass(c)
    val totalAmount = productPricesByTaxClass.map(_._2).foldLeft(0L) { _ + _ }

    val prices = productPricesByTaxClass map {
      case (taxClass, tcTotal) => {
        val pct100 = new BigDecimal("100")
        val discAmount = (new BigDecimal(tcTotal * priceable.pct)).divide(pct100, c.mc)
        (taxClass, rounding(discAmount.negate()))
      }
    }
    Success(prices)
  }

}
