package plus.coding.ckrecom

import java.math.BigDecimal

object Tax {
  /**
   * A TaxSystem is responsible to
   *  determine a TaxRate for a given tax class.
   */
  type TaxSystem = TaxClass => TaxRate

  sealed trait TaxClass {
    val key: String
  }

  case class SimpleTax(key: String) extends TaxClass {
  }

  case object FreeTax extends TaxClass {
    val key = "no tax"
  }
}

