package plus.coding.ckrecom.tax

import java.math.{ BigDecimal, MathContext }

case class TaxRate(num: Int, denom: Int) {
  require(denom != 0)

  private val numBigDec = new BigDecimal(num)
  private val denomBigDec = new BigDecimal(denom)

  /** Calculates the tax value using this rate */
  def taxValue(netAmount: BigDecimal)(implicit mc: MathContext): BigDecimal = {
    netAmount.multiply(numBigDec, mc).divide(denomBigDec, mc)
  }

  def taxValueFromGross(grossAmount: BigDecimal)(implicit mc: MathContext): BigDecimal = {
    grossAmount.multiply(numBigDec, mc).divide(denomBigDec.add(numBigDec, mc), mc)
  }

  /** Returns the gross amount corresponding to the given net amount */
  def grossAmount(netAmount: BigDecimal)(implicit mc: MathContext): BigDecimal = {
    netAmount.add(taxValue(netAmount), mc)
  }
  def netAmount(grossAmount: BigDecimal)(implicit mc: MathContext): BigDecimal = {
    grossAmount.subtract(taxValueFromGross(grossAmount), mc)
  }

  /** Compares this tax rate with another one.
    * Returns -1 if this rate is less than the other, 0 if both
    * are equal, and 1 if this rate is greater.
    */
  def compare(other: TaxRate): Int = {
    val x = num.toLong * other.denom.toLong - other.num.toLong * denom.toLong
    x.signum
  }

  override def toString: String = {
    "%d/%d".format(num, denom)
  }
}

object TaxRate {
  def free: TaxRate = new TaxRate(0, 100)
}
