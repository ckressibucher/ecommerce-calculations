package plus.coding.ckrecom

import java.math.{MathContext, RoundingMode}

import plus.coding.ckrecom.impl.PriceCalculations

import scala.collection.immutable.{Map, Seq}

object Cart {

  /** A type used for calculation results. It holds the information of:
    *
    * @param sum       The total sum of all items of a given tax class
    * @param taxAmount The calculated tax amount for this class
    */
  case class TaxClassSumAndTaxAmount(sum: Long, taxAmount: Long)

  /** Builds a cart from a sequence of not-yet-calculated items (`CartItemPre` instances),
    * and tax price mode. This is the recommended *main* method to build and calculate
    * a cart.
    *
    * @return Either a successfully calculated cart ([[SuccessCart]]) or an [[InterimCart]] with errors.
    */
  def fromItems[T <: CartItemCalculator[_, U], U: TaxSystem](items: Seq[T], mode: PriceMode.Value)
                                                            (implicit mc: MathContext): CartResult[U] = {
    val initCart = InterimCart(mode, Seq.empty)
    val cart = (initCart /: items) {
      case (c: InterimCart[U], item: CartItemCalculator[_, _]) =>
        val calculatedItem: CartContentItem[_, U] = item.finalPrices(c) match {
          case Left(err) => FailedItem(item.priceable, err, item.isMainItem)
          case Right(priceMap) => SuccessItem(item.priceable, priceMap, item.isMainItem)
        }
        c.addContent(calculatedItem)
    }
    cart.validate
  }

  /** Returns a string describing the contents of this object.
    */
  def debugString(cart: SuccessCart[_]): String = {
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

sealed trait CartTrait[T] {
  self: PriceCalculations =>

  implicit val mc: MathContext
  implicit val taxSystem: TaxSystem[T]

  val mode: PriceMode.Value

  /** Returns a map from tax class `T` to a tuple holding the sum price and the tax amount.
    *
    * The sum price is the sum of all prices of this tax class `T` in the cart's price mode.
    * The tax amount is the amount of taxes calculated based on the tax class and the price sum.
    */
  def totalsByTaxClass: Map[T, Long] = allPricesByTaxClass(this)

  /** Returns the prices of the 'successful' content items.
    * In a [[SuccessCart]], all items are 'successful'.
    */
  def contentPrices: Seq[Map[T, Long]] = contentItems collect {
    case SuccessItem(_, priceResults, _) => priceResults
  }

  def contentItems: Seq[CartContentItem[_, T]]

  def taxes(rounding: RoundingMode = RoundingMode.HALF_UP): Map[T, Cart.TaxClassSumAndTaxAmount] = {
    totalsByTaxClass map {
      case (taxCls, price) =>
        val rate = taxSystem.rate(taxCls)
        val taxAmount = mode match {
          case PriceMode.PRICE_GROSS => rate.taxValueFromGross(price)
          case PriceMode.PRICE_NET => rate.taxValue(price)
        }
        (taxCls, Cart.TaxClassSumAndTaxAmount(price, taxAmount.setScale(0, rounding).longValue()))
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
    val itemSums = for (item <- contentPrices) yield item.values.sum
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

/** A final cart by definition only contains successfully calculated items
  *
  */
case class SuccessCart[T](override val mode: PriceMode.Value,
                          contents: Seq[SuccessItem[_, T]])
                         (implicit val mc: MathContext, val taxSystem: TaxSystem[T])
  extends CartTrait[T] with PriceCalculations {

  override def contentItems: Seq[CartContentItem[_, T]] = contents
}

/** A [[CartTrait]] implementation with items that may contain errors */
case class InterimCart[T](override val mode: PriceMode.Value,
                          contents: Seq[CartContentItem[_, T]] = Seq.empty)
                         (implicit val mc: MathContext, val taxSystem: TaxSystem[T])
  extends CartTrait[T] with PriceCalculations {

  /** Adds an item to the cart.
    *
    * This does not validate the content. You should call
    * validate the final cart instance whenever you
    * update a cart.
    */
  def addContent(item: CartContentItem[_, T]): InterimCart[T] = {
    copy(contents = contents ++ Seq(item))
  }

  override def totalsByTaxClass: Map[T, Long] =
    allPricesByTaxClass(this)

  def contentItems: Seq[CartContentItem[_, T]] = contents

  def successItems: Seq[SuccessItem[_, T]] = contents.collect {
    case s: SuccessItem[_, T] => s
  }

  /** Returns the failed items */
  def failedItems: Seq[FailedItem[_, T]] = contents.collect {
    case f: FailedItem[_, T] => f
  }

  /** checks for failed items and returns either this instance, or a [[SuccessCart]]
    */
  def validate: Either[InterimCart[T], SuccessCart[T]] = {
    if (failedItems.nonEmpty) {
      Left(this)
    } else {
      Right(SuccessCart(mode, successItems))
    }
  }
}
