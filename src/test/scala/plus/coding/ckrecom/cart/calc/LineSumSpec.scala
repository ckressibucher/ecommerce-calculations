package plus.coding.ckrecom.cart.calc

import org.scalatest._
import org.javamoney.moneta._
import plus.coding.ckrecom._
import java.math.MathContext
import javax.money._
import java.math.BigDecimal
import scala.collection.immutable._
import scala.util.{ Try, Failure, Success }

class LineSumSpec extends FlatSpec with Matchers {

  import cart._

  implicit val mc = java.math.MathContext.DECIMAL32

  val usd = javax.money.Monetary.getCurrency("USD")

  val initFinalPrice: Try[Seq[TaxedPrice]] = Failure(new RuntimeException("not yet calculated"))

  val taxSystem10Pct: Tax.TaxSystem = (tc: Tax.TaxClass) => TaxRate(10, 100)

  case class SimpleProduct(prices: Map[CurrencyUnit, BigDecimal], taxClass: Tax.TaxClass) extends Product {
    def netPrice(cur: CurrencyUnit) = prices.get(cur)
    def currencies = prices.keys.toList
  }

  class TestPriceService(implicit val mc: MathContext, val taxsystem: Tax.TaxSystem) extends PriceService {
    def priceFor(product: Product, cur: CurrencyUnit, qty: BigDecimal = new BigDecimal(1)): Try[BigDecimal] = {
      product.netPrice(cur) match {
        case Some(p) => new Success(p)
        case None => new Failure(throw new RuntimeException("no price found"))
      }
    }

    def grossPriceFor(product: Product, cur: CurrencyUnit, qty: BigDecimal): Try[BigDecimal] = {
      val rate = taxsystem(product.taxClass)
      priceFor(product, cur) map { rate.grossAmount(_) }
    }
  }

  def sumTotals(cart: Cart): BigDecimal = {
    cart.contents.foldLeft(BigDecimal.ZERO) { (acc: BigDecimal, item: CartContentItem) =>
      def sum(xs: Seq[BigDecimal]): BigDecimal = xs.foldLeft(BigDecimal.ZERO)(_.add(_, mc))

      def itemSum = item.finalPrices match {
        case Success(ps) => sum(ps.map(_._1))
        case Failure(_) => BigDecimal.ZERO
      }
      acc.add(itemSum, mc)
    }
  }

  def buildSimpleCart(ps: List[Product]): Cart = {
    val contents = ps map { p =>
      val line = Priceable.Line(p, new BigDecimal(1))
      CartContentItem(line, initFinalPrice)
    }
    new Cart(contents, usd, PRICE_NET)
  }

  "The LineSum cart calculator" should "sum up all line item prices" in {
    implicit val taxsystem = taxSystem10Pct

    val priceService = new TestPriceService
    val calculator = new LineSum(priceService)

    val productA = SimpleProduct(Map(usd -> new BigDecimal("100")), Tax.FreeTax)
    val productB = SimpleProduct(Map(usd -> new BigDecimal("50")), Tax.FreeTax)

    val cart = buildSimpleCart(List(productA, productB))
    val processedCart = calculator(cart)
    sumTotals(processedCart) should be(new BigDecimal("150"))
  }
  
  it should "add taxes if cart mode is PRICE_GROSS" in {
    implicit val taxsystem = taxSystem10Pct // use a taxsystem that adds 10 %, ignoring the tax class

    val priceService = new TestPriceService
    val calculator = new LineSum(priceService)

    val productA = SimpleProduct(Map(usd -> new BigDecimal("100")), Tax.FreeTax)
    val productB = SimpleProduct(Map(usd -> new BigDecimal("50")), Tax.FreeTax)

    val cart = buildSimpleCart(List(productA, productB)).copy(mode = PRICE_GROSS)
    val processedCart = calculator(cart)
    sumTotals(processedCart) should be(new BigDecimal("165"))
  }
}