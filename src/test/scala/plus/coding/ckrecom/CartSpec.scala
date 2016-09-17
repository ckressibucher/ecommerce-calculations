package plus.coding.ckrecom

import java.math.{ BigDecimal, MathContext }

import scala.collection.immutable._

import org.scalatest._

import plus.coding.ckrecom.impl.Priceable._
import TaxSystem._

class CartSpec extends FlatSpec with Matchers with CartTestHelper {

  implicit val mc = MathContext.DECIMAL32

  implicit val productImpl = defaultProductImpl

  "A cart" should "be constructed from a list of items, currency, and price mode" in {
    implicit val taxSystem = DefaultTaxSystem

    val items = Seq.empty
    val cart = Cart.fromItems(items, PriceMode.PRICE_GROSS)
    cart match {
      case Right(successCart) => successCart.grandTotal() should be(0L)
      case Left(errs)         => fail("Cart calculation failed, first error was: " ++ errs.head.mkString("\n"))
    }
  }

  it should "be constructed from a list of final calculated items" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val product = SimpleProduct[T](new BigDecimal("100"), taxCls10Pct)
    val prices: Map[T, Long] = Map(taxCls10Pct -> 100L)
    val item = CartContentItem(Line(product, new BigDecimal("1")), Right(prices))
    val cart = Cart(PriceMode.PRICE_NET, List(item))

    cart.grandTotal() should be(100L)
  }

  it should "return a map of taxes by tax class" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val taxCls5Pct = new SimpleTax(5, 100)

    // A product with 10 pct tax class
    val productA = SimpleProduct[T](new BigDecimal("1000"), taxCls10Pct)
    val taxedPriceA: Map[T, Long] = Map(taxCls10Pct -> 1000L)
    val itemA = CartContentItem(Line(productA, new BigDecimal("1")), Right(taxedPriceA))

    // A product with no tax
    val productB = SimpleProduct[T](new BigDecimal("1000"), taxFree)
    val taxedPriceB: Map[T, Long] = Map(taxFree -> 1000L)
    val itemB = CartContentItem(Line(productB, new BigDecimal("1")), Right(taxedPriceB))

    // A product with 5 pct tax
    val productC = SimpleProduct[T](new BigDecimal("1000"), taxCls5Pct)
    val taxedPriceC: Map[T, Long] = Map(taxCls5Pct -> 1000L)
    val itemC = CartContentItem(Line(productC, new BigDecimal("1")), Right(taxedPriceC))

    // A discount, for which we apply taxes evenly distributed to both tax classes used
    val discount = new FixedDiscount("code", 200)
    val discountPrices: Map[T, Long] = Map(taxCls5Pct -> -100L, taxCls10Pct -> -100L)
    val discountItem = CartContentItem(discount, Right(discountPrices))

    val cart = Cart(PriceMode.PRICE_NET, List(itemA, itemB, itemC, discountItem))

    val taxMap = cart.taxes()
    taxMap.get(taxCls5Pct) should be(Some(50 - 5)) // itemB - discount
    taxMap.get(taxCls10Pct) should be(Some(100 - 10)) // itemA - discount
    taxMap.get(taxFree) should be(Some(0))
  }

  "Validation: The Cart.fromItems method" should "fail and return a list of errors if the cart is not valid" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    // a product for our cart...
    val product = SimpleProduct[T](new BigDecimal("100"), taxCls10Pct)
    val cartLine = new Line(product, new BigDecimal("1"))

    // .. which somehow produces a calculation error
    val err = "some error"
    val errorCalculation = new CartItemCalculator[Line[_, _], T] {
      def priceable = cartLine
      def finalPrices(c: CartBase[T]): PriceResult[T] = Left(err)
    }

    val items: Seq[CartItemCalculator[Line[_, _], T]] = Seq(errorCalculation)
    val cart = Cart.fromItems[CartItemCalculator[Line[_, _], T], T](items, PriceMode.PRICE_GROSS)

    cart match {
      case Left(errs) => errs.head should be(err)
      case Right(_)   => fail("expected the cart calculations to fail!")
    }
  }

}
