package plus.coding.ckrecom
package impl

import java.math.BigDecimal
import java.math.MathContext
import scala.util.Try
import javax.money.CurrencyUnit

/** Helper for `LineCalc` to determine the price for a product.
  */
trait PriceService {

  /** Returns the real net price for the product,
    * potentially also considering context information like
    * customer, etc.
    *
    * This default implementation directly returns the net price defined in the product
    *
    * It's the responsibility of the implementing
    * class to define and prepare the required
    * context information.
    *
    * The request may fail, e.g. if the currency is not supported.
    */
  def priceFor[T](product: Product[T], cur: CurrencyUnit, qty: BigDecimal = new BigDecimal(1))(implicit ts: TaxSystem[T], mc: MathContext): Try[BigDecimal] = {
    Try {
      product.netPrice(cur).get
    }
  }

  /** Returns the real gross price for the product.
    *
    * Note that you normally want either the net or
    * the gross price for calculations, not both.
    * If the tax for each product is calculated separately,
    * the sum of all taxes might not be the same as the
    * tax applied to the total net price (due to rounding).
    *
    * The request may fail, e.g. if the currency is not supported.
    */
  def grossPriceFor[T](product: Product[T], cur: CurrencyUnit, qty: BigDecimal = new BigDecimal(1))(implicit ts: TaxSystem[T], mc: MathContext): Try[BigDecimal] = {
    val taxSystem = implicitly[TaxSystem[T]]
    priceFor(product, cur, qty) map { netAmount: BigDecimal =>
      val rate = taxSystem.rate(product.taxClass)
      rate.grossAmount(netAmount)
    }
  }
}

object PriceService {

  /** the default service is a concrete class using the trait's implementations
    */
  object DefaultPriceService extends PriceService {}
}
