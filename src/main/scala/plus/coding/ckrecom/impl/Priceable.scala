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

object Priceable {
  /** A Priceable:
    * A cart line holding a product and a quantity
    */
  case class Line[T: TaxSystem](product: Product[T], qty: BigDecimal)

  object Line {
    /** constructor using `Int` value as quantity */
    def apply[T: TaxSystem](product: Product[T], qty: Int): Line[T] = {
      val qtyBD = new BigDecimal(qty)
      apply(product, qtyBD)
    }
  }

  /** A Priceable:
    * A discount code
    */
  case class FixedDiscount(code: String, amount: Long)

  /** A Priceable:
    * A discount as percentage of the product prices
    *
    * @param code The discount code
    * @param pct The percentage disount to apply, e.g. pct=30 reduces a 10 USD to 7 USD
    */
  case class PctDiscount(code: String, pct: Int)

  /** A Priceable:
    * A shipping fee, represented by a string key
    */
  case class Shipping(key: String)

  /** A Priceable:
    * A fixed-amount fee (amount is either a gross or net value)
    */
  case class Fee(amount: MonetaryAmount, mode: PriceMode.Value)
}
