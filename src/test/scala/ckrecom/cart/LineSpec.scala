import ckrecom.cart.{Line, Cart}
import ckrecom._
import java.math.BigDecimal
import org.javamoney.moneta.Money
import org.scalatest._

class LineSpec extends FlatSpec with Matchers {

  // the default price service simply multiplies
  // the product price with the quantity
  import Cart.defaultPriceService

  "A cart Line" should "calculate net price" in {
    val unitPrice = Money.of(100, "USD")
    val product = SimpleProduct("sku", unitPrice, FreeTax(), "name")
    val qty = new BigDecimal(3)
    val line = new Line(qty, product)
    line.price should be(Money.of(300, "USD"))
  }

  it should "use implicit price service to calculate price" in  {
    val unitPrice = Money.of(100, "USD")
    // use a "special price" here...
    implicit val priceService = new PriceService {
      override def priceFor(p: Product) = unitPrice
    }
    val product = SimpleProduct("sku", Money.of(110, "EUR"), FreeTax(), "name")
    val qty = new BigDecimal(5)
    val line = new Line(qty, product)
    line.price should be(Money.of(500, "USD"))
  }

  it should "calculate gross price" in {
    implicit val taxsystem = new TaxSystem {
      override def taxFor(tc: TaxClass) = TaxRate(20, 100)
    }
    val unitPrice = Money.of(100, "USD")
    val product = SimpleProduct("sku", unitPrice, FreeTax(), "name")
    val qty = new BigDecimal(3)
    val line = new Line(qty, product)
    // 3 * 100 USD * 120%
    line.grossPrice should be(Money.of(360, "USD"))
  }

}
