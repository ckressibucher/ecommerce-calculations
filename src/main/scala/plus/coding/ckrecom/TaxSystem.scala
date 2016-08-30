package plus.coding.ckrecom

/** A `TaxSystem` is used to calculate a `TaxRate` for every supported tax class `C`.
  * Note that `C` instances are used as Map keys to identify tax classes, so you
  * probably want to override `hashCode` and `equals` methods for `C` or use case classes.
  *
  * (the idea of using a `TaxSystem[C]` over a simple trait `TaxClass` is, that a
  * TaxSystem[C] better expresses the idea of a system containing a set of supported
  * tax classes. See the default implementation `TaxSystem.DefaultTaxSystem`).
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
