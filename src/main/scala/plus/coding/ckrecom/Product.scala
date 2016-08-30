package plus.coding.ckrecom

import java.math.BigDecimal
import scala.collection.immutable.Seq

/** A product defines as a bare minimum
  * - a tax class
  * - a net price
  *
  * Important: it's not intended to define a gross Price in a product,
  * to not depend on a tax system. The gross price can be calculated
  * by a PriceService.
  *
  * This product interface also does not define a currency. It is
  * expected that all products work with the same currency. The
  * user's implementation may convert currencies to achieve this.
  *
  * @tparam T type of a taxclass
  */
abstract class Product[T] {

  /** Returns the base net price for this article.
    */
  def netPrice: Option[BigDecimal]

  /** The tax class defined for this product.
    */
  def taxClass: T
}
