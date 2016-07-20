package plus.coding.ckrecom

import scala.math.Numeric
import Tax.TaxClass
import java.math.{BigDecimal => JavaBigDec}

trait Product {

  /**
   * Returns the base net price for this article.
   */
  def netPrice: JavaBigDec

  def taxClass: TaxClass
}

object Product {

  case class SimpleProduct[T](
      id: String,
      netPrice: T,
      taxClass: TaxClass,
      name: String) extends Product {

  }
}



