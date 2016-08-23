package plus.coding.ckrecom

import java.math.BigDecimal
import scala.util.Try
import javax.money.CurrencyUnit
import plus.coding.ckrecom.tax.TaxSystem

/** Responsible to determine the final price for a product.
  */
abstract class PriceService[T: TaxSystem] {

  /** Returns the real net price for the product,
    * considering also context information like
    * customer, etc.
    *
    * It's the responsibility of the implementing
    * class to define and prepare the required
    * context information.
    *
    * The request may fail, e.g. if the currency is not supported.
    */
  def priceFor(product: Product[T], cur: CurrencyUnit, qty: BigDecimal = new BigDecimal(1)): Try[BigDecimal]

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
  def grossPriceFor(product: Product[T], cur: CurrencyUnit, qty: BigDecimal = new BigDecimal(1)): Try[BigDecimal]
}
