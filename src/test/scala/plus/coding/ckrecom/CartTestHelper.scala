package plus.coding.ckrecom

import java.math.BigDecimal
import javax.money._
import plus.coding.ckrecom.impl._
import scala.collection.immutable._
import TaxSystem._
import plus.coding.ckrecom.impl.PriceService

/** Some utility values, methods and classes to test cart logic */
trait CartTestHelper {

  val usdollar = Monetary.getCurrency("USD")

  val taxCls10Pct = new TaxSystem.SimpleTax(10, 100)
  val taxFree: TaxSystem.DefaultTaxClass = TaxSystem.FreeTax

  /** A taxsystem that always returns 10 percent tax rate */
  val taxSystem10Pct: TaxSystem[DefaultTaxClass] = new TaxSystem[DefaultTaxClass] {
    def rate(tc: DefaultTaxClass): TaxRate = TaxRate(10, 100)
  }

  val taxSystemDefault: TaxSystem[DefaultTaxClass] = DefaultTaxSystem

  def bigDec(value: String): BigDecimal = new BigDecimal(value)

  def buildSimpleProduct[T: TaxSystem](price: String, tc: T): Product[T] = {
    val p = Map(usdollar -> new BigDecimal(price))
    SimpleProduct(p, tc)
  }

  /** Concrete class for PriceService trait */
  class TestPriceService[T: TaxSystem] extends PriceService {
  }

  /** The most basic implementation of a Product */
  case class SimpleProduct[T: TaxSystem](prices: Map[CurrencyUnit, BigDecimal], taxClass: T) extends Product[T] {
    def netPrice(cur: CurrencyUnit) = prices.get(cur)
    def currencies = prices.keys.toList
  }

}
