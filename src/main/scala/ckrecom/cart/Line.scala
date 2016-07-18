package ckrecom.cart

import ckrecom._
import java.math.BigDecimal
import javax.money._

class Line(val qty: BigDecimal, val product: Product) {

  /** Returns the net price for this line */
  def price(implicit priceService: PriceService): MonetaryAmount = {
    priceService.priceFor(product).multiply(qty)
  }

  def grossPrice(implicit ps: PriceService, taxsystem: TaxSystem): MonetaryAmount = {
    val rate = taxsystem.taxFor(product.taxClass)
    rate.grossAmount(price(ps))
  }
}

object Line {
}

