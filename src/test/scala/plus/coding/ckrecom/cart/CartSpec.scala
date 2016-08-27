package plus.coding.ckrecom.cart

import java.math.{ BigDecimal, MathContext }

import scala.collection.immutable.Seq
import scala.util.{ Failure, Success }

import org.scalatest._

import Priceable.{ FixedDiscount, Line }
import plus.coding.ckrecom.PriceMode
import plus.coding.ckrecom.cart.calc.CartTestHelper
import plus.coding.ckrecom.tax.TaxSystem.{ DefaultTaxClass, DefaultTaxSystem, SimpleTax }

class CartSpec extends FlatSpec with Matchers with CartTestHelper {

  implicit val mc = MathContext.DECIMAL32

  "A cart" should "be constructed from a list of items, currency, and price mode" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val items = Seq.empty
    val cart = Cart.fromItems(items, usdollar, PriceMode.PRICE_GROSS)
    cart.grandTotal() should be(0L)
  }

  it should "be constructed from a list of final calculated items" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val product = SimpleProduct[T](Map(usdollar -> new BigDecimal("100")), taxCls10Pct)
    val prices: Map[T, Long] = Map(taxCls10Pct -> 100L)
    val item = CartContentItem(Line(product, new BigDecimal("1")), Success(prices))
    val cart = Cart(usdollar, PriceMode.PRICE_NET, List(item))

    cart.grandTotal() should be(100L)
  }

  it should "return a map of taxes by tax class" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val taxCls5Pct = new SimpleTax(5, 100)

    // A product with 10 pct tax class
    val productA = SimpleProduct[T](Map(usdollar -> new BigDecimal("1000")), taxCls10Pct)
    val taxedPriceA: Map[T, Long] = Map(taxCls10Pct -> 1000L)
    val itemA = CartContentItem(Line(productA, new BigDecimal("1")), Success(taxedPriceA))

    // A product with no tax
    val productB = SimpleProduct[T](Map(usdollar -> new BigDecimal("1000")), taxFree)
    val taxedPriceB: Map[T, Long] = Map(taxFree -> 1000L)
    val itemB = CartContentItem(Line(productB, new BigDecimal("1")), Success(taxedPriceB))

    // A product with 5 pct tax
    val productC = SimpleProduct[T](Map(usdollar -> new BigDecimal("1000")), taxCls5Pct)
    val taxedPriceC: Map[T, Long] = Map(taxCls5Pct -> 1000L)
    val itemC = CartContentItem(Line(productC, new BigDecimal("1")), Success(taxedPriceC))

    // A discount, for which we apply taxes evenly distributed to both tax classes used
    val discount = new FixedDiscount("code", 200)
    val discountPrices: Map[T, Long] = Map(taxCls5Pct -> -100L, taxCls10Pct -> -100L)
    val discountItem = CartContentItem(discount, Success(discountPrices))

    val cart = Cart(usdollar, PriceMode.PRICE_NET, List(itemA, itemB, itemC, discountItem))

    val taxMap = cart.taxes()
    taxMap.get(taxCls5Pct) should be(Some(50 - 5)) // itemB - discount
    taxMap.get(taxCls10Pct) should be(Some(100 - 10)) // itemA - discount
    taxMap.get(taxFree) should be(Some(0))
  }

  "The cart's `validate` method" should "return en empty Sequence if cart is valid" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val cart = Cart(usdollar, PriceMode.PRICE_NET)
    cart.validate should be(Seq.empty)
  }

  it should "return a list of errors if the cart is not valid" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val product = SimpleProduct[T](Map(usdollar -> new BigDecimal("100")), taxCls10Pct)
    val err = new CalculationException("some error")
    val item = CartContentItem(Line(product, new BigDecimal("1")), Failure(err))
    val cart = Cart(usdollar, PriceMode.PRICE_NET, List(item))

    cart.validate should be(Seq(err))
  }

}
