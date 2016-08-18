package plus.coding.ckrecom.cart.calc

import org.scalatest._
import org.javamoney.moneta._
import plus.coding.ckrecom._
import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.Tax._
import java.math.MathContext
import javax.money._
import java.math.BigDecimal
import scala.collection.immutable._
import scala.util.{ Try, Failure, Success }

import Priceable.Line

class LineSumSpec extends FlatSpec with Matchers with CartTestHelper {

  implicit val mc = java.math.MathContext.DECIMAL32
  
  def lineSumCalculator(implicit ts: TaxSystem): LineSum =
    new LineSum(new TestPriceService)

  "The LineSum cart calculator" should "add prices for all line items" in {
    implicit val taxsystem = taxSystem10Pct
    
    val calculator = lineSumCalculator

    val productA = SimpleProduct(Map(usdollar -> new BigDecimal("100")), Tax.FreeTax)
    val productB = SimpleProduct(Map(usdollar -> new BigDecimal("50")), Tax.FreeTax)
    val lineA = Line(productA, bigDec("1"))
    val cipA = CartItemPre(lineA, calculator)
    val lineB = Line(productB, bigDec("1"))
    val cipB = CartItemPre(lineB, calculator)
    val products = Seq(cipA, cipB);

    val cart = Cart(usdollar, PriceMode.PRICE_NET, products)
    sumTotals(cart) should be(new BigDecimal("150"))
  }

  it should "add taxes if cart mode is PRICE_GROSS" in {
    implicit val taxsystem = taxSystem10Pct // use a taxsystem that adds 10 %, ignoring the tax class

    val calculator = lineSumCalculator
    
    val line = Line(buildSimpleProduct(price = "100"), bigDec("1"))
    val cart = Cart(Seq.empty, usdollar, PriceMode.PRICE_GROSS)
    calculator(cart, line) should be(Success(Seq( (bigDec("110"), FreeTax) )))
  }
  
  it should "allow mixed tax classes" in {
    implicit val taxsystem = taxSystemZeroOr10Pct // zero for FreeTax, 10 percent for SimpleTax
    
    val calculator = lineSumCalculator
    
    val products = List(
        Line(buildSimpleProduct("100", FreeTax), bigDec("2")), // line sum: 200
        Line(buildSimpleProduct("50", SimpleTax("10 pct")), bigDec("1"))) // line sum: 55
    val preItems: List[CartItemPre[Line]] = products map { CartItemPre(_, calculator) }
        
    val cart = Cart(usdollar, PriceMode.PRICE_GROSS, preItems)
    sumTotals(cart) should be(new BigDecimal("255"))
  }
}