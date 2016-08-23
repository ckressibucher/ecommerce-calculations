package plus.coding.ckrecom.tax

import java.math.BigDecimal
import scala.util.Try

/** An implementation of a tax system must provide a type for tax classes (`C`)
  * and a function to convert instances of that type to `TaxRate`s.
  */
trait TaxSystem[C] {

  /** Transforms a given tax class to a `TaxRate`.
    */
  def rate(taxClass: C): TaxRate
}

object TaxSystem {

  // a simple tax classes implementation
  sealed trait DefaultTaxClass
  case object FreeTax extends DefaultTaxClass
  case class SimpleTax(num: Int, denom: Int) extends DefaultTaxClass

  object DefaultTaxSystem extends TaxSystem[DefaultTaxClass] {

    /** The default tax system implementation */
    override def rate(tc: DefaultTaxClass): TaxRate = tc match {
      case FreeTax               => TaxRate.free
      case SimpleTax(num, denom) => TaxRate(num, denom)
    }
  }

}
