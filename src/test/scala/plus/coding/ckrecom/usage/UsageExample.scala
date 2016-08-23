package plus.coding.ckrecom
package usage

import java.math.BigDecimal
import javax.money.Monetary
import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.cart.Priceable._
import plus.coding.ckrecom.cart.calc._

object UsageExample extends App {

  import scala.language.implicitConversions

  val usDollar = Monetary.getCurrency("USD")

  implicit def intToBigDec(v: Int): BigDecimal = new BigDecimal(v)

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
  //val priceService = defaultPriceService

  // === Building a cart ====================

  // To build a cart, we have to define an item calculator for each item (articles, discounts, ..).
  // Note: The order of lines is important for some kinds of lines, as they may
  // depend on previously defined lines. In our example, it is important that the articles
  // are defined before the percentage discount.

  //val preCalculatedItems = lines map { LineCalc(_,) }
}
