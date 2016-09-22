package plus.coding.ckrecom

import java.math.{MathContext, RoundingMode}

import plus.coding.ckrecom.impl.PriceCalculations

import scala.collection.immutable.{Map, Seq}

object Cart {

  /** A type used for calculation results. It holds the information of:
    *
    * @param sum The total sum of all items of a given tax class
    * @param taxAmount The calculated tax amount for this class
    */
  case class TaxClassSumAndTaxAmount(sum: Long, taxAmount: Long)

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
        c.addContent(CartContentItem(item.priceable, prices, item.isMainItem))
    }
    validate(cart) match {
      case Nil  => Right(cart) // TODO the result is has no errors, so `CartContentItem.results` could be simplified in the final result
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
      case (errs, CartContentItem(_, Right(_), _)) => errs
      case (errs, CartContentItem(_, Left(e), _))  => e +: errs
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

  val contents: Seq[CartContentItem[_, T]] = Seq.empty

  val mode: PriceMode.Value

  /** Returns a map from tax class `T` to a tuple holding the sum price and the tax amount.
    *
    * The sum price is the sum of all prices of this tax class `T` in the cart's price mode.
    * The tax amount is the amount of taxes calculated based on the tax class and the price sum.
    */
  def taxes(rounding: RoundingMode = RoundingMode.HALF_UP): Map[T, Cart.TaxClassSumAndTaxAmount] = {
    allPricesByTaxClass(this) map {
      case (taxCls, price) =>
        val rate = taxSystem.rate(taxCls)
        val newPrice = mode match {
          case PriceMode.PRICE_GROSS => rate.taxValueFromGross(price)
          case PriceMode.PRICE_NET   => rate.taxValue(price)
        }
        (taxCls, Cart.TaxClassSumAndTaxAmount(price, newPrice.setScale(0, rounding).longValue()))
    }
  }

  def taxSum(roundingMode: RoundingMode = RoundingMode.HALF_UP): Long = {
    taxes(roundingMode).foldLeft(0L) {
      case (acc, (_, Cart.TaxClassSumAndTaxAmount(_, taxAmount))) => acc + taxAmount
    }
  }

  /** Calculates the grand total of the cart, in the given price mode.
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

  def netTotal(roundingMode: RoundingMode = RoundingMode.HALF_UP): Long = {
    mode match {
      case PriceMode.PRICE_NET => grandTotal()
      case PriceMode.PRICE_GROSS => grandTotal() - taxSum(roundingMode)
    }
  }

  def grossTotal(roundingMode: RoundingMode = RoundingMode.HALF_UP): Long = {
    mode match {
      case PriceMode.PRICE_NET => grandTotal() + taxSum(roundingMode)
      case PriceMode.PRICE_GROSS => grandTotal()
    }
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
