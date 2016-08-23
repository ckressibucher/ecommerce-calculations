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

class LineCalc[T: TaxSystem](val line: Line[T], val priceService: PriceService[T]) extends CartItemPre[Line[T], T] {
  val priceable = line

  val taxSystem = implicitly[TaxSystem[T]]

  def finalPrices(c: Cart[T]): PriceResult[T] = {
    calc(line.product, line.qty, c.currency, c.mode, c.mc) map { p: BigDecimal =>
      Seq(TaxedPrice(p, line.product.taxClass))
    }
  }

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
