package plus.coding.ckrecom.cart.calc

import org.scalatest._
import org.javamoney.moneta._
import plus.coding.ckrecom._
import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.tax._
import java.math.MathContext
import javax.money._
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

    val productA = SimpleProduct[T](Map(usdollar -> new BigDecimal("100")), SimpleTax(1, 10))
    val productB = SimpleProduct[T](Map(usdollar -> new BigDecimal("50")), SimpleTax(1, 10))
    val lineItems = List(productA, productB) map { p: SimpleProduct[T] =>
      val line = Line(p, bigDec("1"))
      lineSumCalculator(line)
    }

    val cart = Cart[CartItemPre[_, T], T](lineItems, usdollar, PriceMode.PRICE_NET)
    sumTotals(cart) should be(150L)
  }

  it should "add taxes if cart mode is PRICE_GROSS" in {
    implicit val taxsystem = taxSystemDefault
    type T = DefaultTaxClass

    val line = Line(buildSimpleProduct[T](price = "100", taxCls10Pct), bigDec("1"))
    val cart = Cart(Seq.empty, usdollar, PriceMode.PRICE_GROSS)
    val lineCalc = lineSumCalculator(line)
    lineCalc.finalPrices(cart) should be(Success(Seq(TaxedPrice[T](bigDec("110"), taxCls10Pct))))
  }

  it should "allow mixed tax classes" in {
    implicit val taxsystem = taxSystemDefault
    type T = DefaultTaxClass

    val products = List(
      Line(buildSimpleProduct[T]("100", taxFree), bigDec("2")), // line sum: 200
      Line(buildSimpleProduct[T]("50", taxCls10Pct), bigDec("1"))) // line sum: 55
    val preItems: List[CartItemPre[_, T]] = products map { lineSumCalculator(_) }

    val cart = Cart[CartItemPre[_, T], T](preItems, usdollar, PriceMode.PRICE_GROSS)
    sumTotals(cart) should be(255L)
  }
}
