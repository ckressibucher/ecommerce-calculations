package plus.coding.ckrecom

import java.math.BigDecimal
import plus.coding.ckrecom.impl._
import scala.collection.immutable._
import TaxSystem._

/** Some utility values, methods and classes to test cart logic */
trait CartTestHelper {

  val taxCls10Pct = new TaxSystem.SimpleTax(10, 100)
  val taxFree: TaxSystem.DefaultTaxClass = TaxSystem.FreeTax

  /** A taxsystem that always returns 10 percent tax rate */
  val taxSystem10Pct: TaxSystem[DefaultTaxClass] = new TaxSystem[DefaultTaxClass] {
    def rate(tc: DefaultTaxClass): TaxRate = TaxRate(10, 100)
  }

  val taxSystemDefault: TaxSystem[DefaultTaxClass] = DefaultTaxSystem

  def bigDec(value: String): BigDecimal = new BigDecimal(value)

  def buildSimpleProduct[T: TaxSystem](price: String, tc: T): Product[T] = {
    SimpleProduct(new BigDecimal(price), tc)
  }

  /** Concrete class for PriceService trait */
  class TestPriceService extends PriceService {
  }

  /** The most basic implementation of a Product */
  case class SimpleProduct[T: TaxSystem](price: BigDecimal, taxClass: T) extends Product[T] {
    def netPrice = Some(price)
  }

}
