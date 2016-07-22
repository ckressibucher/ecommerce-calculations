package plus.coding.ckrecom.cart.calc

import plus.coding.ckrecom.cart._
import plus.coding.ckrecom._
import Priceable._
import java.math.BigDecimal
import javax.money.CurrencyUnit
import scala.collection.immutable._
import scala.util.{Try, Failure, Success}

class LineSum(val priceService: PriceService) extends CartCalculator {
  
  def apply(c: Cart): Cart = {
    val newContents = c.contents.map {
      case i @ CartContentItem(Line(product, qty), _) => {
        val price = calc(product, qty, c.currency, c.mode)
        val newPrices = price map { p => Seq((p, product.taxClass)) }
        i.copy(finalPrices = newPrices)
      }
      case i => i
    }
    c.copy(contents = newContents)(mc = c.mc)
  }
  
  private def calc(product: Product, qty: BigDecimal, cur: CurrencyUnit, mode: PriceMode): Try[BigDecimal] = {
      val singlePrice = mode match {
        case PRICE_NET =>
          priceService.priceFor(product, cur, qty)
        case PRICE_GROSS =>
          priceService.grossPriceFor(product, cur, qty)
      }
      singlePrice.map(_.multiply(qty))
    }
}