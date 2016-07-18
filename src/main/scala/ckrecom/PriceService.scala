package ckrecom

import java.math.BigDecimal
import javax.money._

trait PriceService {

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
  ): MonetaryAmount

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
  )(implicit taxsystem: TaxSystem): MonetaryAmount
}

class DefaultPriceService extends PriceService {

  // Just return the base net price.
  // Other, more advanced services could return
  // different prices, based on current customer or
  // other parameters...
  def priceFor(
    product: Product,
    qty: BigDecimal = new BigDecimal(1)
  ): MonetaryAmount = {
    product.netPrice
  }

  def grossPriceFor(
    product: Product,
    qty: BigDecimal = new BigDecimal(1)
  )(implicit taxsystem: TaxSystem): MonetaryAmount = {
    val rate = taxsystem.taxFor(product.taxClass)
    product.netPrice.multiply(rate.num).divide(rate.denum)
  }

}
