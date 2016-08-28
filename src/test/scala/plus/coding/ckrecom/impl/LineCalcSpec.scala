package plus.coding.ckrecom
package impl

import org.scalatest._
import java.math.MathContext
import java.math.BigDecimal
import scala.collection.immutable._
import scala.util.{ Try, Failure, Success }

import Priceable.Line
import TaxSystem._

class LineCalcSpec extends FlatSpec with Matchers with CartTestHelper {

  implicit val mc = java.math.MathContext.DECIMAL32
  implicit val rounding = Rounding.defaultRounding

  def lineSumCalculator[T: TaxSystem](line: Line[T]): LineCalc[T] =
    new LineCalc(line, new TestPriceService)

  "The LineSum cart calculator" should "add prices for all line items" in {
    implicit val taxsystem = taxSystemDefault
    type T = DefaultTaxClass

    val productA = SimpleProduct[T](new BigDecimal("100"), taxCls10Pct)
    val productB = SimpleProduct[T](new BigDecimal("50"), taxCls10Pct)
    val lineItems = List(productA, productB) map { p: SimpleProduct[T] =>
      val line = Line(p, bigDec("1"))
      lineSumCalculator(line)
    }

    val cart: CartResult[T] = Cart.fromItems[CartItemCalculator[_, T], T](lineItems, PriceMode.PRICE_NET)
    cart.right.get.grandTotal() should be(150L)
  }

  it should "add taxes if cart mode is PRICE_GROSS" in {
    implicit val taxsystem = taxSystemDefault
    type T = DefaultTaxClass

    val line = Line(buildSimpleProduct[T](price = "100", taxCls10Pct), bigDec("1"))
    val cart = Cart.fromItems(Seq.empty, PriceMode.PRICE_GROSS)
    val lineCalc = lineSumCalculator(line)
    lineCalc.finalPrices(cart.right.get) should be(Success(Map(taxCls10Pct -> 110L)))
  }

  it should "allow mixed tax classes" in {
    implicit val taxsystem = taxSystemDefault
    type T = DefaultTaxClass

    val products = List(
      Line(buildSimpleProduct[T]("100", taxFree), bigDec("2")), // line sum: 200
      Line(buildSimpleProduct[T]("50", taxCls10Pct), bigDec("1"))) // line sum: 55
    val preItems: List[CartItemCalculator[_, T]] = products map { lineSumCalculator(_) }

    val cart = Cart.fromItems[CartItemCalculator[_, T], T](preItems, PriceMode.PRICE_GROSS)
    cart.right.get.grandTotal() should be(255L)
  }

  it should "use the rounding service to round line sums" in {
    implicit val taxsystem = taxSystemDefault
    implicit val rounding = Rounding.to5Cents // round down to 5 cents

    type T = DefaultTaxClass

    val line = Line(buildSimpleProduct[T](price = "99", taxFree), bigDec("1"))
    val cart = Cart.fromItems(Seq.empty, PriceMode.PRICE_NET)
    val lineCalc = new LineCalc(line, new TestPriceService)
    lineCalc.finalPrices(cart.right.get) should be(Success(Map(taxFree -> 95L)))
  }
}
