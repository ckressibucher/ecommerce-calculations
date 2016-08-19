package plus.coding.ckrecom.cart.calc

import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.cart.Priceable._
import plus.coding.ckrecom._
import Priceable._
import java.math.BigDecimal
import javax.money.CurrencyUnit
import scala.collection.immutable._
import scala.util.{ Try, Failure, Success }

class FixedDiscountCalc(val priceable: FixedDiscount) extends CartItemPre[FixedDiscount] {

  def finalPrices(c: Cart): Try[Seq[TaxedPrice]] = {
    val price = (priceable.amount.negate(), Tax.FreeTax) // TODO use tax class from products of the cart...
    Success(Seq(price))
  }

}

class PctDiscountCalc(val priceable: PctDiscount) extends CartItemPre[PctDiscount] {

  def finalPrices(c: Cart): Try[Seq[TaxedPrice]] = {
    val productPricesByTaxClass = pricesByTaxClass(c)
    val totalAmount = productPricesByTaxClass.map(_._2).foldLeft(BigDecimal.ZERO) { _.add(_, c.mc) }

    val prices = productPricesByTaxClass.toList map {
      case (taxClass, tcTotal) => {
        val discAmount = tcTotal.multiply(new BigDecimal(priceable.pct)).divide(new BigDecimal(100))
        (discAmount.negate(), taxClass)
      }
    }
    Success(prices)
  }

  /** Analyzes the final prices of all `Line` items, and builds a map TaxClass -> Summed amounts */
  def pricesByTaxClass(cart: Cart): Map[Tax.TaxClass, BigDecimal] = {
    val init = Map[Tax.TaxClass, BigDecimal]()
    cart.contents.foldLeft(init) {
      case (acc, CartContentItem(Line(_, _), Success(prices))) =>
        prices.foldLeft(acc) {
          case (accLine, (amnt, taxCls)) => {
            val newAmount = accLine.getOrElse(taxCls, BigDecimal.ZERO).add(amnt, cart.mc)
            accLine.updated(taxCls, newAmount)
          }
        }
      case (acc, _) => acc
    }
  }
}
