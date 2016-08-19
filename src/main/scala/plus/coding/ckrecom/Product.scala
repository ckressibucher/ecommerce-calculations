package plus.coding.ckrecom

import Tax.TaxClass
import java.math.{ BigDecimal => JavaBigDec }
import javax.money.CurrencyUnit
import scala.collection.immutable._
import scala.math.Numeric

/** A product defines as a bare minimum
  * - a tax class
  * - a list of supported currencies
  * - a net price for each of the supported currencies.
  *
  * Important: it's not intended to define a gross Price in a product,
  * to not depend on a tax system. The gross price can be calculated
  * by a PriceService.
  */
trait Product {

  /** Returns the base net price for this article.
    */
  def netPrice(cur: CurrencyUnit): Option[JavaBigDec]

  def taxClass: TaxClass

  def currencies: Seq[CurrencyUnit]
}
