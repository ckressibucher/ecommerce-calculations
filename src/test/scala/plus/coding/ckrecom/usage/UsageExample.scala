package plus.coding.ckrecom
package usage

import java.math.{ BigDecimal, MathContext }
import javax.money.Monetary
import plus.coding.ckrecom.impl.Priceable._
import plus.coding.ckrecom.impl._
import scala.collection.immutable._
import scala.util.{ Left, Right }
import javax.money.CurrencyUnit

/** Usage example how to create and calculate a cart.
  *
  * This example can be run with sbt using `sbt test:run`.
  */
object UsageExample extends App {

  // In this example, we use usDollar as currency.
  val usDollar = Monetary.getCurrency("USD")

  // The most important type we have to define
  // is the tax class to use.
  //
  // There must be TaxSystem implementation for this (tax class) type,
  // which needs to be available implicitly.
  //
  // We use the `DefaultTaxClass` and `DefaultTaxSystem` here.
  type TaxCls = TaxSystem.DefaultTaxClass
  implicit val taxSystem = TaxSystem.DefaultTaxSystem

  // A type alias to simplify some type annotations.
  // A `CartItemPre` holds some cart content (e.g. a product) and
  // an algorithm to calculate prices. It depends on a tax system.
  //
  // Here, we define our specialized type for our tax class `TaxCls`
  // and generalize over the cart content (using `_`).
  type CalcItem = CartItemCalculator[_, TaxCls]

  // we use `Int`s in our `Article`s to define prices
  type Cents = Int

  /** A simple implementation of a `Product`.
    *
    * A `Product` is an instance of a sellable good. It is abstract in the
    * library, as it's expected that the user needs to define some more
    * data for his products.
    */
  case class Article(name: String, price: Cents) extends Product[TaxCls] {
    // all our articles only support this one currency...
    def currencies: Seq[CurrencyUnit] = Seq(usDollar)

    def netPrice(cur: javax.money.CurrencyUnit): Option[java.math.BigDecimal] = cur match {
      case x if x == usDollar => Some(new BigDecimal(price))
      case _                  => None
    }

    def taxClass: TaxSystem.DefaultTaxClass = new TaxSystem.SimpleTax(10, 100) // 10 % tax
  }

  /** By implementing a `CartSystem`, we define all properties needed by
    * the library to do its calculations.
    *
    * See the `CartSystem` type to see what abstract members it defines.
    */
  val exampleProductsAndDiscounts = new CartSystem[TaxCls] {

    // for some (java) BigDecimal calculations, we need a `MathContext` available
    implicit val mc: MathContext = MathContext.DECIMAL128

    implicit val taxSystem = TaxSystem.DefaultTaxSystem

    val currency: CurrencyUnit = usDollar

    val priceMode: PriceMode.Value = PriceMode.PRICE_GROSS

    // here we define the cart lines for our articles.
    // we only need to define the lines containing product and quantity,
    // the `CartSystem` uses the default calculation logic (i.e. builds a `LineCalc` item
    // from the `Line`s).
    override def buildCartLines: Seq[Line[TaxCls]] = {
      // We start with defining some articles:
      val chair = Article("chair", 80 * 100) // a chair with a price of 80 USD (net price!)
      val table = Article("table", 250 * 100) // a table, 250 USD
      // Next, we create cart lines by defining quantities for each product we want to buy
      // (1 table and 4 chairs)
      Line(chair, 4) :: Line(table, 1) :: Nil
    }

    // This method is used to add any adjustment items, such as discounts or fees.
    // They are appended to the items generated from the result of `buildCartLines`.
    override def buildAdjustmentItems: Seq[CalcItem] = {
      implicit val rounding = Rounding.defaultRounding

      // We also have a discount of 10 %.
      val percentageDiscount = PctDiscount("give me 10 %", 10)

      // .. and additionally a fixed discount or 20 USD
      val fixedDiscount = FixedDiscount("20 dollar less", 2000)

      Seq(
        new FixedDiscountCalc[TaxCls](fixedDiscount, TaxSystem.FreeTax),
        new PctDiscountCalc(percentageDiscount))
    }
  }

  // Run this example, and print the resulting cart
  exampleProductsAndDiscounts.run match {
    case Right(c) => println(Cart.debugString(c))
    case Left(errs) => {
      println("The cart could not be calculated successfully. Errors:")
      errs.foreach { x => println(x.getMessage) }
    }
  }
}
