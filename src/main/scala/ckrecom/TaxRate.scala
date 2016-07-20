package plus.coding.ckrecom

import java.math.{BigDecimal => JavaBigDec}

// TODO use scala's BigDecimal instead??
// not sure how MathContext works there...
case class TaxRate(num: Int, denom: Int) {
  require(denom != 0)
  
  implicit val mc: java.math.MathContext

  private val numBigDec = new JavaBigDec(num)
  private val denomBigDec = new JavaBigDec(denom)

  /** Calculates the tax value using this rate */
  def taxValue(netAmount: JavaBigDec): JavaBigDec = {
    netAmount.multiply(numBigDec, mc).divide(denomBigDec, mc)
  }

  def taxValueFromGross(grossAmount: JavaBigDec): JavaBigDec = {
    grossAmount.multiply(numBigDec, mc).divide(denomBigDec.add(numBigDec, mc), mc)
  }

  /** Returns the gross amount corresponding to the given net amount */
  def grossAmount(netAmount: JavaBigDec): JavaBigDec = {
    netAmount.add(taxValue(netAmount), mc)
  }
  def netAmount(grossAmount: JavaBigDec): JavaBigDec = {
    grossAmount.subtract(taxValueFromGross(grossAmount), mc)
  }

  override def toString = {
    "%d/%d".format(num, denom)
  }
}

