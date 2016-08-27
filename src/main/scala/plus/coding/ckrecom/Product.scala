package plus.coding.ckrecom

import tax.TaxSystem
import java.math.BigDecimal
import javax.money.CurrencyUnit
import scala.collection.immutable.Seq
import scala.math.Numeric

/** A product defines as a bare minimum
  * - a tax class
  * - a list of supported currencies
  * - a net price for each of the supported currencies.
  *
  * Important: it's not intended to define a gross Price in a product,
  * to not depend on a tax system. The gross price can be calculated
  * by a PriceService.
  *
  * @tparam T type of a taxclass
  */
abstract class Product[T: TaxSystem] {

  /** Returns the base net price for this article.
    */
  def netPrice(cur: CurrencyUnit): Option[BigDecimal]

  /** The tax class defined for this product.
    */
  def taxClass: T

  /** Returns a list of currencies for which a price is defined
    * (or can be calculated somehow)
    */
  def currencies: Seq[CurrencyUnit]
}
