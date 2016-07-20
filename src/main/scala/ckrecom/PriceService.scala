package plus.coding.ckrecom

import java.math.BigDecimal
import Tax.TaxSystem

trait PriceService {
  
  implicit val mc: java.math.MathContext

  /** Returns the real net price for the product,
   *  considering also context information like
   *  customer, etc.
   *
   *  It's the responsibility of the implementing
   *  class to define and prepare the required
   *  context information
   */
  def priceFor(
    product: Product,
    qty: BigDecimal = new BigDecimal(1)
  ): BigDecimal

  /** Returns the real gross price for the product.
   *
   *  Note that you normally want either the net or
   *  the gross price for calculations, not both.
   *  If the tax for each product is calculated separately,
   *  the sum of all taxes might not be the same as the
   *  tax applied to the total net price (due to rounding).
   */
  def grossPriceFor(
    product: Product,
    qty: BigDecimal = new BigDecimal(1)
  )(implicit taxsystem: TaxSystem): BigDecimal
}

class DefaultPriceService extends PriceService {

  // Just return the base net price.
  // Other, more advanced services could return
  // different prices, based on current customer or
  // other parameters...
  def priceFor(
    product: Product,
    qty: BigDecimal = new BigDecimal(1)
  ): BigDecimal = {
    product.netPrice
  }

  def grossPriceFor(
    product: Product,
    qty: BigDecimal = new BigDecimal(1)
  )(implicit taxsystem: TaxSystem): BigDecimal = {
    val rate = taxsystem(product.taxClass)
    rate.grossAmount(product.netPrice)
  }

}
