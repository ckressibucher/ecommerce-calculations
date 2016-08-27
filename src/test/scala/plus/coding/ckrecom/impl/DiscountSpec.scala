package plus.coding.ckrecom.cart.calc

import org.scalatest._
import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.tax._
import plus.coding.ckrecom._
import java.math.BigDecimal
import scala.collection.immutable._
import scala.util.{ Try, Success, Failure }
import Priceable.Line

import TaxSystem._

class DiscountSpec extends FlatSpec with Matchers with CartTestHelper {

  implicit val mc = java.math.MathContext.DECIMAL32
  implicit val rounding = Rounding.defaultRounding

  def lineSumCalc[T: TaxSystem](line: Line[T]) = new LineCalc(line, new TestPriceService)

  "The FixedDiscount calculator" should "subtract a fixed amount" in {
    implicit val taxsystem = taxSystemDefault
    type T = DefaultTaxClass
    val fixedDisc = Priceable.FixedDiscount("ten-less", bigDec("10"))
    val calculator = new FixedDiscountCalc(fixedDisc, taxFree)

    val cart = Cart(usdollar, PriceMode.PRICE_NET)
    calculator.finalPrices(cart) should be(Success(Map(taxFree -> -10L)))
  }

  "The PercentageDiscount calculator" should "apply percentage prices per tax class" in {
    implicit val taxsystem = taxSystemDefault
    type T = DefaultTaxClass

    val disc = Priceable.PctDiscount("ten-pct", 10)
    val calculator = new PctDiscountCalc(disc)

    val products = List(
      Line(buildSimpleProduct[T]("100", taxFree), bigDec("1")),
      Line(buildSimpleProduct[T]("100", SimpleTax(1, 10)), bigDec("1")))
    val preItems: Seq[CartItemPre[Line[T], T]] = products map { lineSumCalc(_) }

    val cart = Cart.fromItems[CartItemPre[_, T], T](preItems, usdollar, PriceMode.PRICE_NET)

    val discPrices = calculator.finalPrices(cart.right.get)
    val expectedPrices: Map[T, Long] = Map(
      FreeTax -> -10L,
      SimpleTax(1, 10) -> -10L)
    discPrices should be(Success(expectedPrices))
  }
}
