package plus.coding.ckrecom

import org.scalatest._
import java.math.{ BigDecimal, MathContext, RoundingMode }

class RoundingSpec extends FlatSpec with Matchers {

  "The Rounding implementation" should "round a value using the given rounding mode" in {
    val xs = List("2.0", "-0.5", "7.5", "7.4999999", "99.9") map (new BigDecimal(_))

    val roundingHalfUp = new RoundingImpl(RoundingMode.HALF_UP)
    xs.map(roundingHalfUp(_)) should be(List(2L, -1, 8, 7, 100))

    val roundingCeil = new RoundingImpl(RoundingMode.CEILING)
    xs.map(roundingCeil(_)) should be(List(2L, 0, 8, 8, 100))
  }

  "The default rounding" should "round with HALF_UP" in {
    val xs = List("2.0", "-0.5", "7.5", "7.4999999", "99.9") map (new BigDecimal(_))

    val rounding = Rounding.defaultRounding
    xs.map(rounding(_)) should be(List(2L, -1, 8, 7, 100))
  }

  "The alwaysUp rounding" should "always round up" in {
    val xs = List("2.0", "-0.5", "7.5", "7.4999999", "99.9") map (new BigDecimal(_))

    val rounding = Rounding.alwaysUp
    xs.map(rounding(_)) should be(List(2L, 0, 8, 8, 100))
  }

  "The rounding-to-five rounding" should "round to a multiple of 5" in {
    implicit val mathCxt = MathContext.DECIMAL32
    val xs = List("2.0", "-16", "74.999999", "75.1", "72.49999", "99") map (new BigDecimal(_))

    val roundingHalfUp = new RoundingToFive(RoundingMode.HALF_UP)
    xs.map(roundingHalfUp(_)) should be(List(0L, -15, 75, 75, 70, 100))

    val roundingFloor = new RoundingToFive(RoundingMode.FLOOR)
    xs.map(roundingFloor(_)) should be(List(0L, -20, 70, 75, 70, 95))
  }
}
