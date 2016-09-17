package plus.coding.ckrecom
package impl

import org.scalatest._

import scala.collection.immutable._
import Priceable.Line
import TaxSystem._

class DiscountSpec extends FlatSpec with Matchers with CartTestHelper {

  implicit val mc = java.math.MathContext.DECIMAL32
  implicit val rounding = Rounding.defaultRounding
  implicit val productImpl = defaultProductImpl

  "The FixedDiscount calculator" should "subtract a fixed amount" in {
    implicit val taxsystem = taxSystemDefault
    val fixedDisc = Priceable.FixedDiscount("ten-less", bigDec("10"))
    val calculator = new FixedDiscountCalc(fixedDisc, taxFree)

    val cart = Cart(PriceMode.PRICE_NET)
    calculator.finalPrices(cart) should be(Right(Map(taxFree -> -10L)))
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
