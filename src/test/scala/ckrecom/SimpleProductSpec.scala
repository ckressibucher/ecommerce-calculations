import ckrecom.{TaxClass, SimpleTax, Product, SimpleProduct}
import javax.money._
import org.scalatest._
import org.javamoney.moneta.Money

class SimpleProductSpec extends FlatSpec with Matchers {

  "A SimpleProduct" should "be intantiable" in {
    val price = Money.of(1023, "USD")
    val sp = SimpleProduct("sku-123", price, SimpleTax("A"), "jacket")
    sp.id should be("sku-123")
    sp.netPrice should be(price)
    sp.taxClass should be(SimpleTax("A"))
    sp.name should be("jacket")
  }
}
