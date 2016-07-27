package plus.coding.ckrecom.cart.calc

import org.scalatest._
import org.javamoney.moneta._
import plus.coding.ckrecom._
import plus.coding.ckrecom.cart._
import java.math.MathContext
import javax.money._
import java.math.BigDecimal
import scala.collection.immutable._
import scala.util.{ Try, Failure, Success }

class LineSumSpec extends FlatSpec with Matchers with CartTestHelper {

  implicit val mc = java.math.MathContext.DECIMAL32

  "The LineSum cart calculator" should "sum up all line item prices" in {
    implicit val taxsystem = taxSystem10Pct

    val priceService = new TestPriceService
    val calculator = new LineSum(priceService)

    val productA = SimpleProduct(Map(usdollar -> new BigDecimal("100")), Tax.FreeTax)
    val productB = SimpleProduct(Map(usdollar -> new BigDecimal("50")), Tax.FreeTax)
    val products = List(
      (productA, 1),
      (productB, 1));

    val cart = buildSimpleCart(products)
    val processedCart = calculator(cart)
    sumTotals(processedCart) should be(new BigDecimal("150"))
  }

  it should "add taxes if cart mode is PRICE_GROSS" in {
    implicit val taxsystem = taxSystem10Pct // use a taxsystem that adds 10 %, ignoring the tax class

    val priceService = new TestPriceService
    val calculator = new LineSum(priceService)

    val products = List(
      (buildSimpleProduct(price = "100"), 1),
      (buildSimpleProduct(price = "50"), 1));

    val cart = buildSimpleCart(products).copy(mode = PRICE_GROSS)
    val processedCart = calculator(cart)
    sumTotals(processedCart) should be(new BigDecimal("165"))
  }
}