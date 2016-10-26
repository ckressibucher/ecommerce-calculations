package plus.coding.ckrecom
package impl

import org.scalatest._

import Priceable.Line
import TaxSystem._

class DiscountSpec extends FlatSpec with Matchers with CartTestHelper {

  implicit val mc = java.math.MathContext.DECIMAL32
  implicit val rounding = Rounding.defaultRounding
  implicit val productImpl = defaultProductImpl

  "The FixedDiscount calculator" should "subtract a fixed amount" in {
    implicit val taxsystem = taxSystemDefault
    type T = DefaultTaxClass
    // need a "main item" with price > 0
    val line = Line(SimpleProduct("100", taxCls10Pct), 1)
    val mainItems = new LineCalc(line) :: new LineCalc(Line(SimpleProduct("50", taxFree), 1)) :: Nil
    val fixedDisc = Priceable.FixedDiscount("3-less", bigDec("3"))
    val calculator = new FixedDiscountCalc[T](fixedDisc)

    val cart = Cart.fromItems[CartItemCalculator[_, T], T](mainItems, PriceMode.PRICE_NET).right.get
    calculator.finalPrices(cart) should be(Right(Map(taxCls10Pct -> -2L, taxFree -> -1l)))
  }

  "The PercentageDiscount calculator" should "apply percentage prices per tax class" in {
    implicit val taxsystem = taxSystemDefault
    type T = DefaultTaxClass

    val disc = Priceable.PctDiscount("ten-pct", 10)
    val calculator = new PctDiscountCalc(disc)

    val x = Line(SimpleProduct("100", taxFree), 1)
    val y = Line(SimpleProduct("100", taxCls10Pct), 1)
    val products = x :: y :: Nil

    val preItems = products map { new LineCalc[T, SimpleProduct[T]](_) }

    val cart = Cart.fromItems[CartItemCalculator[_, T], T](preItems, PriceMode.PRICE_NET)

    val discPrices = calculator.finalPrices(cart.right.get)
    val expectedPrices: Map[T, Long] = Map(
      FreeTax -> -10L,
      taxCls10Pct -> -10L)
    discPrices should be(Right(expectedPrices))
  }
}
