package plus.coding.ckrecom.cart

import plus.coding.ckrecom.{Product, PriceMode}
import java.math.BigDecimal
import javax.money.{MonetaryAmount, CurrencyUnit}
import scala.collection.immutable._

/**
 * A Priceable is something that, together with an ItemCalc,
 * can produce a final price which goes into the totals sum.
 */
trait Priceable {
}

object Priceable {
  /** A cart line holding a product and a quantity */
  case class Line(product: Product, qty: BigDecimal) extends Priceable {}

  /** A discount code */
  case class FixedDiscount(code: String, amount: BigDecimal) extends Priceable {}
  
  /** A discount as percentage of the product prices
   *  
   *  @param code The discount code
   *  @param pct The percentage disount to apply, e.g. pct=30 reduces a 10 USD to 7 USD
   */
  case class PctDiscount(code: String, pct: Int) extends Priceable {}

  /** A shipping fee, represented by a string key */
  case class Shipping(key: String) extends Priceable {}

  /** A fixed-amount fee (amount is either a gross or net value) */
  case class Fee(amount: MonetaryAmount, mode: PriceMode.Value) extends Priceable {}
}
