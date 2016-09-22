package plus.coding.ckrecom.impl

import org.scalatest._
import org.scalatest.prop.{PropertyChecks, TableFor2}

class PriceCalculationsSpec extends FlatSpec with Matchers with PropertyChecks {

  object Calculator extends PriceCalculations {}

  "distributeByTaxClass" should "always generate a map with a sum == input price" in {
    // property test
    forAll { (price: Long, distKey: Map[Int, Long]) =>
      // there must exist at least one element with a value > 0 for this property to hold
      whenever(distKey.exists(_._2 > 0)) {
        val result: Map[Int, Long] = Calculator.distributeByTaxClass[Int](price, distKey)
        result.foldLeft(0L) {
          case (acc, (_, partPrice)) => acc + partPrice
        } should be(price)
      }
    }
  }

  it should "return empty list if distributionKey contains only zero or negative values" in {
    val price = 1l // doesn't matter

    val filteredToEmptyMap = Table(
      "distribututionKey",
      Map(1 -> -2l),
      Map(1 -> 0l, 2 -> -2l, 3 -> -1l),
      Map(),
      Map(1 -> 0l, 2 -> 0l)
    )
    forAll(filteredToEmptyMap) { onlyNegativeOrZeroMap =>
      Calculator.distributeByTaxClass(price, onlyNegativeOrZeroMap) should be(Map())
    }
  }
}
