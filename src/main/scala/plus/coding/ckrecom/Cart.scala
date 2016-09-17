package plus.coding.ckrecom

import java.math.{ BigDecimal, MathContext }
import scala.collection.immutable.{ Seq, Map }
import plus.coding.ckrecom.impl.PriceCalculations
import java.math.RoundingMode

object Cart {

  /** Builds a cart from a sequence of not-yet-calculated items (`CartItemPre` instances),
    * and tax price mode. This is the recommended *main* method to build and calculate
    * a cart.
    *
    */
  def fromItems[T <: CartItemCalculator[_, U], U: TaxSystem](items: Seq[T], mode: PriceMode.Value)(implicit mc: MathContext): CartResult[U] = {
    val initCart = new Cart(mode, Seq.empty)
    val cart = (initCart /: items) {
      case (c: Cart[U], item: CartItemCalculator[_, _]) =>
        val prices = item.finalPrices(c)
        c.addContent(CartContentItem(item.priceable, prices))
    }
    validate(cart) match {
      case Nil  => Right(cart)
      case errs => Left(errs)
    }
  }

  /** Checks if the cart contains failed price calculations.
    *
    * Returns a list of errors (or an empty list if the cart is valid).
    */
  def validate[T](cart: CartBase[T]): Seq[String] = {
    val result = Seq.empty[String]
    (result /: cart.contents) {
      case (errs, CartContentItem(_, Right(_))) => errs
      case (errs, CartContentItem(_, Left(e)))  => e +: errs
    }
  }

  /** Returns a string describing the contents of this object.
    */
  def debugString(cart: CartBase[_]): String = {
    val title = "Debugging cart:\n==============="
    val pMode = "Price mode: " ++ cart.mode.toString()
    val contentStr = ("contents:\n---------\n" /: cart.contents) {
      case (acc, item) => acc ++ (" - " ++ item.toString + '\n')
    }
    val taxesStr = ("Taxes:\n------------\n" /: cart.taxes()) {
      case (acc, (cls, amnt)) => acc ++ (cls.toString ++ " : " ++ amnt.toString() + '\n')
    }
    val totalSum = "Total: " ++ cart.grandTotal().toString()
    List(title, pMode, contentStr, taxesStr, totalSum).mkString("\n")
  }

}

abstract class CartBase[T: TaxSystem] extends PriceCalculations {

  implicit val mc: MathContext
  val taxSystem = implicitly[TaxSystem[T]]

  case class Contents[T](cs: Seq[CartContentItem[_, T]])

  val contents: Seq[CartContentItem[_, T]] = Seq.empty

  val mode: PriceMode.Value

  def taxes(rounding: RoundingMode = RoundingMode.HALF_UP): TaxTotals[T] = {
    allPricesByTaxClass(this) map {
      case (taxCls, price) =>
        val rate = taxSystem.rate(taxCls)
        val newPrice = mode match {
          case PriceMode.PRICE_GROSS => rate.taxValueFromGross(price)
          case PriceMode.PRICE_NET   => rate.taxValue(price)
        }
        (taxCls, newPrice.setScale(0, rounding).longValue())
    }
  }

  /** Calculates the grand total of the cart.
    */
  def grandTotal(): Long = {
    val itemSums = for {
      item <- contents
      prices = item.results match {
        case Left(_)                 => 0L
        case Right(ps: Map[_, Long]) => ps.values.sum
      }
    } yield prices
    itemSums.sum
  }
}

/** The main cart class. Represents a cart with already calculated prices.
  *
  */
case class Cart[T: TaxSystem](
  override val mode: PriceMode.Value,
  override val contents: Seq[CartContentItem[_, T]] = Seq.empty)(implicit val mc: MathContext)
    extends CartBase[T] {

  /** Adds an item to the cart.
    *
    * This does not validate the content. You should call
    * validate the final cart instance whenever you
    * update a cart.
    */
  def addContent(item: CartContentItem[_, T]): Cart[T] = {
    copy(contents = contents ++ Seq(item))
  }
}
