package plus.coding.ckrecom.cart.calc

import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.cart.Priceable._
import plus.coding.ckrecom._
import Priceable._
import java.math.BigDecimal
import javax.money.CurrencyUnit
import scala.collection.immutable._
import scala.util.{ Try, Failure, Success }
import plus.coding.ckrecom.tax.TaxSystem
import scala.util.Failure
import java.math.MathContext

/** Applies a fixed discount, using the cheapest tax class of any of the products in cart (or the fallbackTaxClass when no
  * products are in the cart).
  */
class FixedDiscountCalc[T: TaxSystem](val priceable: FixedDiscount[T], fallbackTaxClass: T)
    extends CartItemPre[FixedDiscount[T], T] with PriceCalculations {

  def finalPrices(c: CartBase[T]): PriceResult[T] = {
    val taxClass = cheapestTaxClass(c).getOrElse(fallbackTaxClass)
    val price = TaxedPrice(-1 * priceable.amount, taxClass)
    Success(Seq(price))
  }

}

// TODO Fixed discounts with distributed tax classes, or a fixed tax class

class PctDiscountCalc[T: TaxSystem](val priceable: PctDiscount[T])(implicit val rounding: Rounding)
    extends CartItemPre[PctDiscount[T], T] with PriceCalculations {

  def finalPrices(c: CartBase[T]): Try[Seq[TaxedPrice[T]]] = {
    val productPricesByTaxClass = linePricesByTaxClass(c)
    val totalAmount = productPricesByTaxClass.map(_._2).foldLeft(0L) { _ + _ }

    val prices = productPricesByTaxClass.toList map {
      case (taxClass, tcTotal) => {
        val pct100 = new BigDecimal("100")
        val discAmount = (new BigDecimal(tcTotal * priceable.pct)).divide(pct100, c.mc)
        TaxedPrice(rounding(discAmount.negate()), taxClass)
      }
    }
    Success(prices)
  }

}
