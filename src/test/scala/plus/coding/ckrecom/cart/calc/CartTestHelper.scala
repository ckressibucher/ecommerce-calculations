package plus.coding.ckrecom.cart.calc

import java.math.{ BigDecimal, MathContext }
import javax.money._
import plus.coding.ckrecom._
import plus.coding.ckrecom.cart._
import plus.coding.ckrecom.Tax._
import scala.util.{ Try, Failure, Success }
import scala.collection.immutable._

/** Some utility values, methods and classes to test cart logic */
trait CartTestHelper {

  val usdollar = Monetary.getCurrency("USD")

  /** A taxsystem that always returns 10 percent tax rate */
  val taxSystem10Pct: Tax.TaxSystem = (tc: TaxClass) => TaxRate(10, 100)

  val taxSystemZeroOr10Pct: TaxSystem = (tc: TaxClass) => tc match {
    case FreeTax => TaxRate(0, 100)
    case SimpleTax(_) => TaxRate(10, 100)
  }
  
  def bigDec(value: String): BigDecimal = new BigDecimal(value)

  /** Sums up the totals of the cart.
   *  
   *  Useful to verify calculation logic in tests.
   *  Ignores failed or not yet calculated final item prices.
   */
  def sumTotals(cart: Cart)(implicit mc: MathContext): BigDecimal = {
    // helper method to sum up a sequence of BigDecimal values
    def sum(xs: Seq[BigDecimal]): BigDecimal =
      xs.foldLeft(BigDecimal.ZERO)(_.add(_, mc))
    
    cart.contents.foldLeft(BigDecimal.ZERO) { (acc: BigDecimal, item: CartContentItem) =>
      def itemSum = item.finalPrices match {
        case Success(ps) => sum(ps.map(_._1))
        case Failure(_) => BigDecimal.ZERO
      }
      acc.add(itemSum, mc)
    }
  }

  def buildSimpleProduct(price: String, tc: TaxClass): Product = {
    val p = Map(usdollar -> new BigDecimal(price))
    SimpleProduct(p, tc)
  }
  def buildSimpleProduct(price: String): Product = buildSimpleProduct(price, FreeTax)

  /** A basic implementation of a price service: It just returns the price defined in the product */
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

  /** The most basic implementation of a Product */
  case class SimpleProduct(prices: Map[CurrencyUnit, BigDecimal], taxClass: Tax.TaxClass) extends Product {
    def netPrice(cur: CurrencyUnit) = prices.get(cur)
    def currencies = prices.keys.toList
  }

}