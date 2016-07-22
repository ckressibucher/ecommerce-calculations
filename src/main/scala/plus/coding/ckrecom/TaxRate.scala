package plus.coding.ckrecom

import java.math.{BigDecimal, MathContext}

// TODO use scala's BigDecimal instead??
// not sure how MathContext works there...
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

  override def toString = {
    "%d/%d".format(num, denom)
  }
}

