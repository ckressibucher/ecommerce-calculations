package plus.coding.ckrecom.cart.calc

import java.math.{ BigDecimal, RoundingMode, MathContext }

/** Responsible to round a price amount. Input and output
  * denote the amount of cents (or otherwise smallest unit of a currency).
  */
trait Rounding extends (BigDecimal => Long)

class RoundingImpl(scale: Int, roundingMode: RoundingMode) extends Rounding {
  def apply(in: BigDecimal): Long = {
    in.setScale(scale, roundingMode).longValue()
  }
}

class RoundingToFive(roundingMode: RoundingMode)(implicit val mc: MathContext) extends Rounding {

  val five = new BigDecimal("5")

  def apply(in: BigDecimal): Long = {

    in.divide(five)
      .setScale(0, roundingMode)
      .multiply(five)
      .longValue()
  }
}

object Rounding {

  val defaultRounding = new RoundingImpl(0, RoundingMode.HALF_UP)

  val alwaysUp = new RoundingImpl(0, RoundingMode.CEILING)

  // rounds to a multiple of 5, e.g. for swiss francs/ rappen, using FLOOR rounding
  // mode (so a price of 99 cents gets 95 cents)
  def to5Cents(implicit mc: MathContext) = new RoundingToFive(RoundingMode.FLOOR)
}
