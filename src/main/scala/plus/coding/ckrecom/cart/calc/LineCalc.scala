package plus.coding.ckrecom.cart.calc

import plus.coding.ckrecom.cart._
import plus.coding.ckrecom._
import plus.coding.ckrecom.tax.TaxSystem
import Priceable._
import java.math.BigDecimal
import javax.money.CurrencyUnit
import scala.collection.immutable._
import scala.util.{ Try, Failure, Success }
import java.math.MathContext

/** Calculate the final prices for an item line using a price service.
  */
class LineCalc[T: TaxSystem](val line: Line[T], val priceService: PriceService[T])(implicit val rounding: Rounding) extends CartItemPre[Line[T], T] {
  val priceable = line

  val taxSystem = implicitly[TaxSystem[T]]

  def finalPrices(c: CartBase[T]): PriceResult[T] = {
    val priceTry = calc(line.product, line.qty, c.currency, c.mode, c.mc)
    // round the price and wrap it into the required PriceResult structure
    priceTry map { p: BigDecimal => TaxedPrice(rounding(p), line.product.taxClass) :: Nil }
  }

  /** Calculates one BigDecimal amount for the given product in the given quantity.
    */
  private def calc(product: Product[T], qty: BigDecimal, cur: CurrencyUnit, mode: PriceMode.Value, mc: MathContext): Try[BigDecimal] = {
    val singlePrice = mode match {
      case PriceMode.PRICE_NET =>
        priceService.priceFor(product, cur, qty)
      case PriceMode.PRICE_GROSS =>
        priceService.grossPriceFor(product, cur, qty)
    }
    singlePrice.map(_.multiply(qty))
  }
}
