package plus.coding.ckrecom

import java.math.{BigDecimal, MathContext}

/** A product defines as a bare minimum
  * - a tax class
  * - a net price
  *
  * @tparam T type of a taxclass // TODO should we deine `T` as covariant `+T` ??
  * @tparam P the user defined cart product type. You need to provide a typeclass instance for this
  */
trait Product[T, P] {

  /** Returns the unit net price for this article.
    *
    * The `qty` parameter can be used to produce prices that depend
    * on the order amount
    */
  def netPrice(product: P, qty: BigDecimal): Option[BigDecimal]

  /** The tax class defined for this product.
    */
  def taxClass(product: P): T

  /** Returns the unit gross price for this article.
    *
    * It uses the netPrice value, and applies the tax rate provided by the tax system
    */
  def grossPrice(product: P, qty: BigDecimal)(implicit taxSystem: TaxSystem[T], mc: MathContext): Option[BigDecimal] = {
    val rate = taxSystem.rate(taxClass(product))
    netPrice(product, qty).map(rate.grossAmount)
  }
}

object Product {

  implicit class ProductOps[T, P](self: P)(implicit val ev: Product[T, P]) {
    def netPrice(qty: BigDecimal = new BigDecimal(1)): Option[BigDecimal] =
      ev.netPrice(self, qty)

    def grossPrice(qty: BigDecimal = new BigDecimal(1))
                  (implicit taxSystem: TaxSystem[T], mc: MathContext): Option[BigDecimal] =
      ev.grossPrice(self, qty)

    def taxClass: T =
      ev.taxClass(self)
  }

}

