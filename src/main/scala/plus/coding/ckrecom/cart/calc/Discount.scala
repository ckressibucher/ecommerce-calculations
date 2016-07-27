package plus.coding.ckrecom.cart.calc

import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.cart.Priceable._
import plus.coding.ckrecom._
import Priceable._
import java.math.BigDecimal
import javax.money.CurrencyUnit
import scala.collection.immutable._
import scala.util.{Try, Failure, Success}

class Discount extends CartCalculator {
  
  def apply(c: Cart): Cart = {
    val productPricesByTaxClass = pricesByTaxClass(c)
    val totalAmount = productPricesByTaxClass.map(_._2).foldLeft(BigDecimal.ZERO) { _.add(_, c.mc) }
    val newContents = c.contents.map {
      case i @ CartContentItem(FixedDiscount(code, amount), _) => {
        val price = (amount.negate(), Tax.FreeTax) // TODO use tax class from products...
        i.copy(finalPrices = Success(Seq(price)))
      }
      case i @ CartContentItem(PctDiscount(code, pct),  _) => {
        val prices = productPricesByTaxClass.toList map { _ match {
          case (taxClass, tcTotal) => {
            val disc = tcTotal.multiply(new BigDecimal(pct)).divide(new BigDecimal(100))
            (disc.negate(), taxClass)
          }
        }}
        i.copy(finalPrices = Success(prices))
      }
      case i => i
    }
    c.copy(contents = newContents)(mc = c.mc)
  }
 
  /** Analyzes the final prices of all `Line` items, and builds a map TaxClass -> Summed amounts */
  def pricesByTaxClass(cart: Cart): Map[Tax.TaxClass, BigDecimal] = {
    val init = Map[Tax.TaxClass, BigDecimal]()
    cart.contents.foldLeft(init) { (acc, item) => item match {
      case CartContentItem(Line(_, _), Success(prices)) => {
        prices.foldLeft(acc) { (accLine, p) => p match {
          case (amnt, taxCls) => {
            val newAmount = accLine.getOrElse(taxCls, BigDecimal.ZERO).add(amnt, cart.mc)
            accLine.updated(taxCls, newAmount)
          }
        }}
      }
      case _ => acc 
    }}
  }
}