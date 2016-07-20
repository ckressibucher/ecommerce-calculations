package plus.coding.ckrecom.cart

import plus.coding.ckrecom._
import java.math.BigDecimal
import javax.money._
import Tax.TaxSystem

class Line(val qty: BigDecimal, val product: Product) {

  /** Returns the net price for this line */
  def price(implicit priceService: PriceService): BigDecimal = {
    priceService.priceFor(product).multiply(qty)
  }

  def grossPrice(implicit ps: PriceService, taxsystem: TaxSystem): BigDecimal = {
    val rate = taxsystem(product.taxClass)
    rate.grossAmount(price(ps))
  }
}

object Line {
}

