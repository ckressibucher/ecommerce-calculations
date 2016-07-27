package plus.coding.ckrecom.cart.calc

import org.scalatest._
import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.Tax._
import java.math.BigDecimal

class DiscountSpec extends FlatSpec with Matchers with CartTestHelper {
  
  implicit val mc = java.math.MathContext.DECIMAL32
  implicit val taxsystem = taxSystemZeroOr10Pct
  
  lazy val lineSumCalc = new LineSum(new TestPriceService)
  lazy val discCalculator = new Discount
  lazy val calculator = discCalculator compose lineSumCalc
  
  "The Discount calculator" should "subtract a fixed amount" in {
    val fixedDisc = Priceable.FixedDiscount("ten-less", new BigDecimal("10"))
    val cart = buildSimpleCart(List()).addContent(
        CartContentItem(fixedDisc, initFinalPrice))
        
    val processedCart = calculator(cart)
    
    sumTotals(processedCart) should be(new BigDecimal("-10"))
  }
  
  it should "apply percentage prices per tax class" in {
    val disc = Priceable.PctDiscount("ten-pct", 10)
    
    val products = List(
        (buildSimpleProduct("100", FreeTax), 1),
        (buildSimpleProduct("100", SimpleTax("")), 1))
        
    val cart = buildSimpleCart(products)
                .addContent(CartContentItem(disc, initFinalPrice))
                
    val procCart = calculator(cart)
    sumTotals(procCart) should be(new BigDecimal("180"))
    
    val discPrices = procCart.contents.find {
      case CartContentItem(Priceable.PctDiscount(_, _), _) => true
      case _ => false
    } map { (i: CartContentItem) => i match {
      case CartContentItem(Priceable.PctDiscount(_, _), prices) => prices
    }}
    discPrices.get.isSuccess should be(true)
    discPrices.get.get.forall { _ match {
      case (amount, taxclass) => amount.compareTo(new BigDecimal("-10")) == 0
    }} should be(true)
  }
}