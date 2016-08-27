package plus.coding.ckrecom
package impl

import plus.coding.ckrecom.impl.Priceable._
import java.math.BigDecimal
import scala.collection.immutable._
import scala.util.Try

/** Cal
  * import plus.coding.ckrecom.impl.PriceServiceculate the final prices for an item line using a price service.
  */
class LineCalc[T: TaxSystem](val line: Line[T], val priceService: PriceService)(implicit val rounding: Rounding) extends CartItemCalculator[Line[T], T] {

  val priceable = line

  val taxSystem = implicitly[TaxSystem[T]]

  def finalPrices(c: CartBase[T]): PriceResult[T] = {
    // price for qty == 1
    val singlePrice: Try[BigDecimal] = c.mode match {
      case PriceMode.PRICE_NET =>
        priceService.priceFor(line.product, c.currency, line.qty)(taxSystem, c.mc)
      case PriceMode.PRICE_GROSS =>
        priceService.grossPriceFor(line.product, c.currency, line.qty)(taxSystem, c.mc)
    }

    // price for the whole line
    val linePrice = singlePrice.map(_.multiply(line.qty))

    // round the price and wrap it into the required PriceResult structure
    linePrice map { p: BigDecimal => Map(line.product.taxClass -> rounding(p)) }
  }
}
