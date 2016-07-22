package plus.coding.ckrecom

import java.lang.IllegalArgumentException
import java.math.BigDecimal
import org.scalatest._

class TaxRateSpec extends FlatSpec with Matchers {
  
  implicit val mc = java.math.MathContext.DECIMAL64

  "A TaxRate" should "take an numerator and denumerator" in {
    val tr = TaxRate(10, 100)
    tr.toString should be("10/100")
    intercept[IllegalArgumentException] {
      TaxRate(10, 0) // zero denumerator..
    }
  }

  it should "calculate the tax amount" in {
    // 10 percent tax
    val tr = TaxRate(10, 100)
    val net = new BigDecimal(80)
    tr.taxValue(net).intValue should be(8)
  }

  it should "calculate the gross amount" in {
    val tr = TaxRate(196, 1000)
    val net = BigDecimal.valueOf(100331, 3) // 100.331
    val expected = BigDecimal.valueOf(119995876, 6)
    tr.grossAmount(net) should be(expected)
  }
  
  it should "round irrational numbers according to MathContext" in {
    val tr = TaxRate(1, 3)
    val net = BigDecimal.valueOf(10, 0)
  
    val expected = new BigDecimal("3.333333") //  with 32 bit representation
    tr.taxValue(net)(java.math.MathContext.DECIMAL32) should be(expected)
  }
}
