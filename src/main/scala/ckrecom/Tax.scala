package ckrecom

import java.lang.IllegalArgumentException
import java.math.BigDecimal
import javax.money.MonetaryAmount

sealed trait TaxClass {
  val key: String
}

case class SimpleTax(key: String) extends TaxClass {
}

case object FreeTax extends TaxClass {
  val key = "no tax"
}

/** A TaxSystem is responsible to
 *  determine a TaxRate for a given tax class.
 */
trait TaxSystem {
  def taxFor(cls: TaxClass): TaxRate
}

case class TaxRate(num: Int, denum: Int) {
  require(denum != 0)

  private val numBigDec = new BigDecimal(num)
  private val denumBigDec = new BigDecimal(denum)

  /** Calculates the tax value using this rate */
  def taxValue(netAmount: BigDecimal): BigDecimal = {
    netAmount.multiply(numBigDec).divide(denumBigDec)
  }
  def taxValue(netAmount: MonetaryAmount): MonetaryAmount = {
    netAmount.multiply(numBigDec).divide(denumBigDec)
  }

  /** Returns the gross amount corresponding to the given net amount */
  def grossAmount(netAmount: BigDecimal): BigDecimal = {
    netAmount.add(taxValue(netAmount))
  }
  def grossAmount(netAmount: MonetaryAmount): MonetaryAmount = {
    netAmount.add(taxValue(netAmount))
  }

  override def toString = {
    "%d/%d".format(num, denum)
  }
}
