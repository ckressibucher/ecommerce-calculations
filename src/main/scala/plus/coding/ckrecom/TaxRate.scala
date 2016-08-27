package plus.coding.ckrecom

import java.math.{ BigDecimal, MathContext }

case class TaxRate(num: Int, denom: Int) {
  require(denom != 0)

  private val numBigDec = new BigDecimal(num)
  private val denomBigDec = new BigDecimal(denom)

  /** Calculates the tax value using this rate */
  def taxValue(netAmount: BigDecimal)(implicit mc: MathContext): BigDecimal = {
    netAmount.multiply(numBigDec, mc).divide(denomBigDec, mc)
  }

  def taxValue(netAmount: Long)(implicit mc: MathContext): BigDecimal =
    taxValue(new BigDecimal(netAmount))

  def taxValueFromGross(grossAmount: BigDecimal)(implicit mc: MathContext): BigDecimal = {
    grossAmount.multiply(numBigDec, mc).divide(denomBigDec.add(numBigDec, mc), mc)
  }

  def taxValueFromGross(grossAmount: Long)(implicit mc: MathContext): BigDecimal =
    taxValueFromGross(new BigDecimal(grossAmount))

  /** Returns the gross amount corresponding to the given net amount */
  def grossAmount(netAmount: BigDecimal)(implicit mc: MathContext): BigDecimal = {
    netAmount.add(taxValue(netAmount), mc)
  }

  def netAmount(grossAmount: BigDecimal)(implicit mc: MathContext): BigDecimal = {
    grossAmount.subtract(taxValueFromGross(grossAmount), mc)
  }

  override def toString: String = {
    "%d/%d".format(num, denom)
  }
}

object TaxRate {
  def free: TaxRate = new TaxRate(0, 100)

  implicit object TaxRateOrdering extends Ordering[TaxRate] {
    override def compare(x: TaxRate, y: TaxRate): Int = {
      val res = x.num.toLong * y.denom.toLong - y.num.toLong * x.denom.toLong
      res.signum
    }
  }
}
