package plus.coding.ckrecom
package impl

import java.math.BigDecimal

import plus.coding.ckrecom.impl.Priceable._

import scala.collection.immutable._

/** Applies a fixed discount, using the cheapest tax class of any of the products in cart (or the fallbackTaxClass when no
  * products are in the cart).
  */
class FixedDiscountCalc[T: TaxSystem](val priceable: FixedDiscount)
    extends CartItemCalculator[FixedDiscount, T] with PriceCalculations {

  def finalPrices(c: CartBase[T]): PriceResult[T] = {
    val discPrice = -1 * priceable.amount
    Right(distributeByTaxClass(discPrice, mainItemPricesByTaxClass(c)))
  }

  override def isMainItem: Boolean = false
}

class PctDiscountCalc[T: TaxSystem](val priceable: PctDiscount)(implicit val rounding: Rounding)
    extends CartItemCalculator[PctDiscount, T] with PriceCalculations {

  def finalPrices(c: CartBase[T]): PriceResult[T] = {
    val productPricesByTaxClass = mainItemPricesByTaxClass(c)

    val prices = productPricesByTaxClass map {
      case (taxClass, tcTotal) =>
        val pct100 = new BigDecimal("100")
        val discAmount = new BigDecimal(tcTotal * priceable.pct).divide(pct100, c.mc)
        (taxClass, rounding(discAmount.negate()))
    }
    Right(prices)
  }

  override def isMainItem: Boolean = false
}
