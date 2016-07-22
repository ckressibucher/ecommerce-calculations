package plus.coding.ckrecom.cart

import plus.coding.ckrecom.{Product, PriceMode}
import java.math.BigDecimal
import javax.money.{MonetaryAmount, CurrencyUnit}
import scala.collection.immutable._

/**
 * A Priceable is something that, together with a CartCalculator,
 * can produce a final price which goes into the totals sum.
 * 
 * As a library user, it's your responsibility to provide a
 * CartCalculator for each of the Priceable's you use.
 */
trait Priceable {
}

object Priceable {
  /** A cart line holding a product and a quantity */
  case class Line(product: Product, qty: BigDecimal) extends Priceable {}

  /** A discount code */
  case class Discount(code: String) extends Priceable {}

  /** A shipping fee, represented by a string key */
  case class Shipping(key: String) extends Priceable {}

  /** A fixed-amount fee (amount is either a gross or net value) */
  case class Fee(amount: MonetaryAmount, mode: PriceMode) extends Priceable {}
}
