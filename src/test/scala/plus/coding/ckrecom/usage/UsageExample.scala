package plus.coding.ckrecom
package usage

import java.math.BigDecimal
import javax.money.Monetary
import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.cart.Priceable._
import plus.coding.ckrecom.cart.calc._
import plus.coding.ckrecom.tax.TaxSystem
import scala.collection.immutable.Seq

object UsageExample extends App {

  import scala.language.implicitConversions

  val usDollar = Monetary.getCurrency("USD")

  implicit def intToBigDec(v: Int): BigDecimal = new BigDecimal(v)

  implicit val mathContext: java.math.MathContext = java.math.MathContext.DECIMAL128

  // We start with defining some articles:
  val chair = Article("chair", 80 * 100) // a chair with a price of 80 USD
  val table = Article("table", 250 * 100) // a table, 250 USD

  // We also have a discount of 10 %.
  val percentageDiscount = PctDiscount("give me 10 %", 10)

  // .. and additionally a fixed discount or 20 USD
  val fixedDiscount = FixedDiscount("20 dollar less", 20)

  // Next, we create cart lines by defining quantities for each product we want to buy
  // (1 table and 4 chairs)
  val lines = Line(chair, 4) :: Line(table, 1) :: Nil

  // The prices are not fetched directly from the articles, but are using a price service, which
  // may apply additional rules to the basic prices.
  val priceService = DefaultPriceService

  // === Building a cart ====================

  // To build a cart, we have to define an "item calculator" for each item (articles, discounts, ..).
  // Note: The order of lines is important for some kinds of lines, as they may
  // depend on previously defined lines. In our example, it is important that the articles
  // are defined before the percentage discount.
  val preCalculatedItems: Seq[CartItemPre[_, TaxCls]] = lines map { l: Line[TaxCls] => new LineCalc(l, priceService) }
  val preCalculatedDiscs: Seq[CartItemPre[_, TaxCls]] = Seq(
    new FixedDiscountCalc(fixedDiscount, TaxSystem.FreeTax),
    new PctDiscountCalc(percentageDiscount))

  val cart: Cart[TaxCls] = Cart(preCalculatedItems ++ preCalculatedDiscs, usDollar, PriceMode.PRICE_GROSS)

  // Now we have a cart with calculated items, i.e. for each line, we have a calculated
  println(cart)
}
