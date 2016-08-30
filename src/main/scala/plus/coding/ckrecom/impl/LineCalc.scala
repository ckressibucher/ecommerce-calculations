package plus.coding.ckrecom
package impl

import plus.coding.ckrecom.impl.Priceable._
import java.math.BigDecimal
import scala.collection.immutable._

/** Calculate the final prices for an item line using a price service.
  */
class LineCalc[T: TaxSystem](val line: Line[T], val priceService: PriceService)(implicit val rounding: Rounding) extends CartItemCalculator[Line[T], T] {

  val priceable = line

  val taxSystem = implicitly[TaxSystem[T]]

  def finalPrices(c: CartBase[T]): PriceResult[T] = {
    // price for qty == 1
    val singlePrice: Either[String, BigDecimal] = c.mode match {
      case PriceMode.PRICE_NET =>
        priceService.priceFor(line.product, line.qty)(taxSystem, c.mc)
      case PriceMode.PRICE_GROSS =>
        priceService.grossPriceFor(line.product, line.qty)(taxSystem, c.mc)
    }

    singlePrice.right map { p: BigDecimal =>
      // price for the whole line
      val linePrice = p.multiply(line.qty)
      // round the price and wrap it into the required PriceResult structure
      Map(line.product.taxClass -> rounding(linePrice))
    }
  }
}
