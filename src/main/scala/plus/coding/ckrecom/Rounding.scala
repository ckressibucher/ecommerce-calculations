package plus.coding.ckrecom

import java.math.{BigDecimal, MathContext, RoundingMode}

/** Responsible to round a price amount. Input and output
  * denote the amount of cents (or otherwise smallest unit of a currency).
  */
trait Rounding extends (BigDecimal => Long)

class RoundingImpl(val roundingMode: RoundingMode) extends Rounding {
  def apply(in: BigDecimal): Long = {
    in.setScale(0, roundingMode).longValue()
  }
}

private[ckrecom] class RoundingToFive(roundingMode: RoundingMode)(implicit val mc: MathContext) extends Rounding {

  private val five = new BigDecimal("5")

  def apply(in: BigDecimal): Long = {

    in.divide(five)
      .setScale(0, roundingMode)
      .longValue() * 5
  }
}

/**
  * Contains some rounding implementations
  */
object Rounding {

  val defaultRounding: Rounding = new RoundingImpl(RoundingMode.HALF_UP)

  val alwaysUp: Rounding = new RoundingImpl(RoundingMode.CEILING)

  // rounds to a multiple of 5, e.g. for swiss francs/ rappen, using FLOOR rounding
  // mode (so a price of 99 cents gets 95 cents)
  def to5Cents(implicit mc: MathContext): Rounding = new RoundingToFive(RoundingMode.FLOOR)
}
