package plus.coding.ckrecom.cart

import plus.coding.ckrecom.{Product}
import java.math.BigDecimal
import javax.money.{MonetaryAmount, CurrencyUnit}
import org.javamoney.moneta.Money

trait Priceable {
  def canCurrency(c: CurrencyUnit): Boolean
}

object Priceable {
  /** A cart line holding a product and a quantity */
  case class Line(product: Product, qty: BigDecimal) extends Priceable {}

  /** A discount code */
  case class Discount(code: String) extends Priceable {}

  /** A shipping fee, represented by a string key */
  case class Shipping(key: String) extends Priceable {}

  /** A fixed-amount fee (amount is either a gross or net value) */
  case class Fee(amount: MonetaryAmount, mode: PRICE_MODE) extends Priceable {}
}
