package plus.coding.ckrecom.cart.calc

import java.math.{ BigDecimal, MathContext }
import javax.money._
import plus.coding.ckrecom._
import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.tax._
import scala.util.{ Try, Failure, Success }
import scala.collection.immutable._
import plus.coding.ckrecom.tax.TaxRate

import TaxSystem._

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

  /** Sums up the totals of the cart.
    *
    * Useful to verify calculation logic in tests.
    * Ignores failed or not yet calculated final item prices.
    */
  def sumTotals[T: TaxSystem](cart: Cart[T])(implicit mc: MathContext): Long = {
    cart.contents.foldLeft(0L) { (acc: Long, item: CartContentItem[_, T]) =>
      def itemSum = item.results match {
        case Success(ps) => ps.map(_.price).sum
        case Failure(_)  => 0L
      }
      acc + itemSum
    }
  }

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
