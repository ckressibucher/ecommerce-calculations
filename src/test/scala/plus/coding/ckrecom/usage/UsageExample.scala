package plus.coding.ckrecom
package usage

import java.math.{BigDecimal, MathContext}

import plus.coding.ckrecom.impl.Priceable._
import plus.coding.ckrecom.impl._

import scala.collection.immutable._
import scala.util.{Left, Right}

/** Usage example how to create and calculate a cart.
  *
  * This example can be run with sbt using
  * {{{
  * sbt test:run
  * }}}.
  */
object UsageExample extends App {

  /** The most important type we have to define
    * is the tax class to use.
    *
    * There must be TaxSystem implementation for this (tax class) type,
    * which needs to be available implicitly.
    *
    * We use the [[plus.coding.ckrecom.TaxSystem.DefaultTaxClass]] here,
    * for which a [[TaxSystem]] is implemented ([[TaxSystem.DefaultTaxSystem]]).
    */
  type TaxCls = TaxSystem.DefaultTaxClass

  /* A 10 pct tax class */
  val tax10Pct: TaxCls = TaxSystem.SimpleTax(10, 100)

  /** We use `Int`s in our `Article`s to define prices
    */
  type Cents = Int

  /** An article we want to sell. This can be any type `T` for which an
    * instance of [[Product[TaxCls, T]] exists (see below).
    */
  case class Article(name: String, price: Cents, taxClass: TaxCls)

  /** A simple implementation of a [[plus.coding.ckrecom.Product]]
    *
    * A [[Product]] is an instance of a sellable good. It is abstract in the
    * library, as it's expected that the user needs to define some more
    * data for his products.
    */
  object ArticleProduct extends Product[TaxCls, Article] {
    def netPrice(product: Article, qty: BigDecimal): Option[BigDecimal] =
      Some(new BigDecimal(product.price))

    def taxClass(product: Article): TaxCls =
      product.taxClass
  }

  /** By implementing a [[CartSystem]] we define all properties needed by
    * the library to do its calculations.
    */
  val exampleProductsAndDiscounts = new CartSystem[TaxCls, Article] {

    // for some (java) BigDecimal calculations, we need a `MathContext` available
    override implicit val mc: MathContext = MathContext.DECIMAL128

    // This object implements a tax system for our `TaxCls`.
    override implicit val taxSystem: TaxSystem[TaxCls] = TaxSystem.DefaultTaxSystem

    // The price mode defines how to interpret the price results of the cart items
    override val priceMode: PriceMode.Value = PriceMode.PRICE_GROSS

    /** The implementation that connects our custom [[Article]] class with
      * the data used by the library's [[Product]]
      */
    implicit val productImpl = ArticleProduct

    /** Here we define the cart lines with our articles.
      *
      * We only need to define the lines containing product and quantity,
      * the `CartSystem` uses the default calculation logic (i.e. builds a `LineCalc` item
      * from the `Line`s).
      *
      * Alternatively, we could implement [[BasicCartSystem]] (instead of [[CartSystem]]) and
      * define [[BasicCartSystem.buildCalculationItems]] with custom
      * calculation logic.
      */
    override def buildCartLines: List[Line[TaxCls, Article]] = {
      // define 2 sample articles
      val chair = Article("chair", 80 * 100, tax10Pct) // a chair with a price of 80 USD (net price!)
      val table = Article("table", 250 * 100, tax10Pct) // a table, 250 USD
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
        new FixedDiscountCalc[TaxCls](fixedDiscount),
        new PctDiscountCalc(percentageDiscount))
    }
  }

  // Run this example, and print the resulting cart
  exampleProductsAndDiscounts.run match {
    case Right(c) => println(Cart.debugString(c))
    case Left(errCart) =>
      println("The cart could not be calculated successfully. Errors:")
      errCart.failedItems.map(_.error).foreach(println)
  }
}
