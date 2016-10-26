package plus.coding.ckrecom

import org.scalatest._
import scala.collection.immutable

class TaxSystemSpec extends FlatSpec with Matchers {

  import TaxSystem._

  "DefaultTaxClass instances" should "identify a value of a Map" in {

    // A simple test to make sure we alwas use case classes or classes
    // with equals/hashCode implementations that allow us to use
    // different instances for the "same" tax class in a map.
    val tc1 = FreeTax
    val tc2 = SimpleTax(2, 10)
    val tc3 = SimpleTax(3, 10)
    val tc4 = SimpleTax(4, 10)
    val tc5 = SimpleTax(5, 10)

    // check also immutable map with > 4 items, see
    // http://asyncified.io/2016/08/27/the-case-of-the-immutable-map-and-object-who-forgot-to-override-hashcode/
    val map = Map(tc1 -> 1, tc2 -> 2, tc3 -> 3, tc4 -> 4, tc5 -> 5)
    val imMap = immutable.Map(tc1 -> 1, tc2 -> 2, tc3 -> 3, tc4 -> 4, tc5 -> 5)

    map(FreeTax) should be(1)
    imMap(FreeTax) should be(1)

    map(SimpleTax(2, 10)) should be(2)
    imMap(SimpleTax(2, 10)) should be(2)

    map(SimpleTax(5, 10)) should be(5)
    imMap(SimpleTax(5, 10)) should be(5)
  }

}
