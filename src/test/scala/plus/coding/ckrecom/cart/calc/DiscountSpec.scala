package plus.coding.ckrecom.cart.calc

import org.scalatest._
import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.Tax._
import plus.coding.ckrecom._
import java.math.BigDecimal
import scala.collection.immutable._
import scala.util.{Try, Success, Failure}
import Priceable.Line

class DiscountSpec extends FlatSpec with Matchers with CartTestHelper {
  
  implicit val mc = java.math.MathContext.DECIMAL32
  implicit val taxsystem = taxSystemZeroOr10Pct
  
  def lineSumCalc(line: Line) = new LineCalc(line, new TestPriceService)
  
  "The FixedDiscount calculator" should "subtract a fixed amount" in {
    val fixedDisc = Priceable.FixedDiscount("ten-less", bigDec("10"))
    val calculator = new FixedDiscountCalc(fixedDisc)
        
    val cart = Cart(usdollar, PriceMode.PRICE_NET, Seq.empty[CartItemPre[Priceable]])
    calculator.finalPrices(cart) should be(Success(Seq((bigDec("-10"), Tax.FreeTax))))
  }
  
  "The PercentageDiscount calculator" should "apply percentage prices per tax class" in {
    val disc = Priceable.PctDiscount("ten-pct", 10)
    val calculator = new PctDiscountCalc(disc)
    
    val products = List(
        Line(buildSimpleProduct("100", FreeTax), bigDec("1")),
        Line(buildSimpleProduct("100", SimpleTax("")), bigDec("1")))
    val preItems: List[CartItemPre[Line]] = products map { lineSumCalc(_) }
        
    val cart = Cart(usdollar, PriceMode.PRICE_NET, preItems)
                
    val discPrices = calculator.finalPrices(cart)
    val expectedPrices = Seq( (bigDec("-10"), FreeTax), (bigDec("-10"), SimpleTax("")) )
    discPrices should be(Success(expectedPrices))
  }
}