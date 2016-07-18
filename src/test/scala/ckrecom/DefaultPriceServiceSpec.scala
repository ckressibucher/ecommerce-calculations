import ckrecom.{SimpleProduct, SimpleTax, DefaultPriceService}
import javax.money._
import org.javamoney.moneta.Money
import org.scalatest._

class DefaultPriceServiceSpec extends FlatSpec with Matchers {

  "A DefaultPriceService" should "return the product's base price" in {
    val price = Money.of(100, "EUR")
    val service = new DefaultPriceService
    val product = SimpleProduct("P123", price, SimpleTax("A"), "name")
    service.priceFor(product) should be(price)
  }
}
