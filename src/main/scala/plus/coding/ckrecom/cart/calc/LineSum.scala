package plus.coding.ckrecom.cart.calc

import plus.coding.ckrecom.cart._
import plus.coding.ckrecom._
import Priceable._
import java.math.BigDecimal
import javax.money.CurrencyUnit
import scala.collection.immutable._
import scala.util.{ Try, Failure, Success }
import java.math.MathContext

class LineSum(val priceService: PriceService) extends ItemCalc[Line] {

  def apply(c: Cart, line: Line): Try[Seq[TaxedPrice]] = {
    calc(line.product, line.qty, c.currency, c.mode, c.mc) map { p: BigDecimal =>
      Seq((p, line.product.taxClass))
    }
  }

  private def calc(product: Product, qty: BigDecimal, cur: CurrencyUnit, mode: PriceMode.Value, mc: MathContext): Try[BigDecimal] = {
    val singlePrice = mode match {
      case PriceMode.PRICE_NET =>
        priceService.priceFor(product, cur, qty)
      case PriceMode.PRICE_GROSS =>
        priceService.grossPriceFor(product, cur, qty)
    }
    singlePrice.map(_.multiply(qty))
  }
}
