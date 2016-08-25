package plus.coding.ckrecom.cart

import java.math.BigDecimal
import scala.collection.immutable.Seq
import scala.util.Try
import javax.money.MonetaryAmount
import plus.coding.ckrecom.PriceMode
import plus.coding.ckrecom.Product
import plus.coding.ckrecom.tax.TaxSystem
import plus.coding.ckrecom.tax.TaxSystem
import plus.coding.ckrecom.tax.TaxSystem
import plus.coding.ckrecom.tax.TaxSystem

/** A Priceable is something that, together with an ItemCalc,
  * can produce a final price which goes into the totals sum.
  */
abstract class Priceable[T: TaxSystem] {
}

object Priceable {
  /** A cart line holding a product and a quantity */
  case class Line[T: TaxSystem](product: Product[T], qty: BigDecimal) extends Priceable[T]

  /** A discount code */
  case class FixedDiscount[T: TaxSystem](code: String, amount: Long) extends Priceable[T]

  /** A discount as percentage of the product prices
    *
    * @param code The discount code
    * @param pct The percentage disount to apply, e.g. pct=30 reduces a 10 USD to 7 USD
    */
  case class PctDiscount[T: TaxSystem](code: String, pct: Int) extends Priceable[T]

  /** A shipping fee, represented by a string key */
  case class Shipping[T: TaxSystem](key: String) extends Priceable[T]

  /** A fixed-amount fee (amount is either a gross or net value) */
  case class Fee[T: TaxSystem](amount: MonetaryAmount, mode: PriceMode.Value) extends Priceable[T]
}
