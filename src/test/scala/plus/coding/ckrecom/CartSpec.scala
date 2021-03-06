package plus.coding.ckrecom

import java.math.{BigDecimal, MathContext}

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
      case Right(successCart) =>
        successCart.grandTotal() should be(0L)
      case Left(errorCart) =>
        fail("Cart calculation failed with errors: " +
          errorCart.failedItems.map(_.error).mkString("\n"))
    }
  }

  it should "be constructed from a list of final calculated items" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val product = SimpleProduct[T](new BigDecimal("100"), taxCls10Pct)
    val prices: Map[T, Long] = Map(taxCls10Pct -> 100L)
    val item = SuccessItem(Line(product, new BigDecimal("1")), prices, isMainItem = true)
    val cart = InterimCart(PriceMode.PRICE_NET, List(item))

    cart.grandTotal() should be(100L)
  }

  it should "calculate the gross total from net mode" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val product = SimpleProduct[T](price = "100", taxCls10Pct)
    val prices: Map[T, Long] = Map(taxCls10Pct -> 100L)
    val item = SuccessItem(Line(product, new BigDecimal("1")), prices, isMainItem = true)
    val cart = InterimCart(PriceMode.PRICE_NET, List(item))

    cart.grossTotal() should be(110L)
  }

  it should "calculate the gross total from gross mode" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val product = SimpleProduct[T](price = "100", taxCls10Pct)
    val prices: Map[T, Long] = Map(taxCls10Pct -> 110L)
    val item = SuccessItem(Line(product, new BigDecimal("1")), prices, isMainItem = true)
    val cart = InterimCart(PriceMode.PRICE_GROSS, List(item))

    cart.grossTotal() should be(110L)
  }

  it should "calculate the net total from gross mode" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val product = SimpleProduct[T](price = "100", taxCls10Pct)
    val prices: Map[T, Long] = Map(taxCls10Pct -> 110L)
    val item = SuccessItem(Line(product, new BigDecimal("1")), prices, isMainItem = true)
    val cart = InterimCart(PriceMode.PRICE_GROSS, List(item))

    cart.netTotal() should be(100L)
  }

  it should "calculate the net total from net mode" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val product = SimpleProduct[T](price = "100", taxCls10Pct)
    val prices: Map[T, Long] = Map(taxCls10Pct -> 100L)
    val item = SuccessItem(Line(product, new BigDecimal("1")), prices, isMainItem = true)
    val cart = InterimCart(PriceMode.PRICE_NET, List(item))

    cart.netTotal() should be(100L)
  }

  it should "return a map of taxes by tax class" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    val taxCls5Pct = new SimpleTax(5, 100)

    // A product with 10 pct tax class
    val productA = SimpleProduct[T](new BigDecimal("1000"), taxCls10Pct)
    val taxedPriceA: Map[T, Long] = Map(taxCls10Pct -> 1000L)
    val itemA = SuccessItem(Line(productA, new BigDecimal("1")), taxedPriceA, isMainItem = true)

    // A product with no tax
    val productB = SimpleProduct[T](new BigDecimal("1000"), taxFree)
    val taxedPriceB: Map[T, Long] = Map(taxFree -> 1000L)
    val itemB = SuccessItem(Line(productB, new BigDecimal("1")), taxedPriceB, isMainItem = true)

    // A product with 5 pct tax
    val productC = SimpleProduct[T](new BigDecimal("1000"), taxCls5Pct)
    val taxedPriceC: Map[T, Long] = Map(taxCls5Pct -> 1000L)
    val itemC = SuccessItem(Line(productC, new BigDecimal("1")), taxedPriceC, isMainItem = true)

    // A discount, for which we apply taxes evenly distributed to both tax classes used
    val discount = new FixedDiscount("code", 200)
    val discountPrices: Map[T, Long] = Map(taxCls5Pct -> -100L, taxCls10Pct -> -100L)
    val discountItem = SuccessItem(discount, discountPrices, isMainItem = false)

    val cart = InterimCart(PriceMode.PRICE_NET, List(itemA, itemB, itemC, discountItem))

    val taxMap: Map[T, Long] = cart.taxes().map {
      case (taxCls, Cart.TaxClassSumAndTaxAmount(sum, amnt)) => (taxCls, amnt)
    }
    taxMap.get(taxCls5Pct) should be(Some(50 - 5)) // itemB - discount
    taxMap.get(taxCls10Pct) should be(Some(100 - 10)) // itemA - discount
    taxMap.get(taxFree) should be(Some(0))
  }

  "Validation: The Cart.fromItems method" should "fail and return an InterimCart if the cart is not valid" in {
    type T = DefaultTaxClass
    implicit val taxSystem = DefaultTaxSystem

    // a product for our cart...
    val product = SimpleProduct[T](new BigDecimal("100"), taxCls10Pct)
    val cartLine = new Line(product, new BigDecimal("1"))

    // .. which somehow produces a calculation error
    val err = "some error"
    val errorCalculation = new CartItemCalculator[Line[_, _], T] {
      def priceable = cartLine

      def finalPrices(c: CartTrait[T]): PriceResult[T] = Left(err)

      def isMainItem = false
    }

    val items: Seq[CartItemCalculator[Line[_, _], T]] = Seq(errorCalculation)
    val cart = Cart.fromItems[CartItemCalculator[Line[_, _], T], T](items, PriceMode.PRICE_GROSS)

    cart match {
      case Left(cartResult) => cartResult.failedItems.head.error should be(err)
      case Right(_) => fail("expected the cart calculations to fail!")
    }
  }

}
