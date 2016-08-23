package plus.coding.ckrecom

import java.math.BigDecimal
import javax.money.{ Monetary, CurrencyUnit }
import scala.util.{ Try, Success, Failure }
import plus.coding.ckrecom.tax.TaxSystem
import scala.collection.immutable.Seq

package object usage {

  implicit val taxSystem = TaxSystem.DefaultTaxSystem
  import Implicits.mathContext

  val usdollar: CurrencyUnit = Monetary.getCurrency("USD")

  type Cents = Int

  /** A simple implementation of a `Product`.
    *
    * A `Product` is an instance of a good that's being sold.
    */
  case class Article(name: String, price: Cents) extends Product[TaxSystem.DefaultTaxClass] {
    // all our articles only support this one currency...
    def currencies: Seq[CurrencyUnit] = Seq(usdollar)

    def netPrice(cur: javax.money.CurrencyUnit): Option[java.math.BigDecimal] = cur match {
      case x if x == usdollar => Some(new BigDecimal(price))
      case _                  => None
    }

    def taxClass: TaxSystem.DefaultTaxClass = new TaxSystem.SimpleTax(10, 100) // 10 % tax
  }

  object DefaultPriceService extends PriceService[TaxSystem.DefaultTaxClass] {
    type T = TaxSystem.DefaultTaxClass

    override def priceFor(product: Product[T], cur: CurrencyUnit, qty: BigDecimal = new BigDecimal(1)): Try[BigDecimal] = {
      Try {
        product.netPrice(cur).get
      }
    }

    override def grossPriceFor(product: Product[T], cur: CurrencyUnit, qty: BigDecimal = new BigDecimal(1)): Try[BigDecimal] = {
      priceFor(product, cur, qty) map { netAmount: BigDecimal =>
        val rate = taxSystem.rate(product.taxClass)
        rate.grossAmount(netAmount)
      }
    }
  }
}
