package plus.coding.ckrecom

import java.math.{ BigDecimal, MathContext }
import plus.coding.ckrecom.impl.{ LineCalc, PriceService }
import plus.coding.ckrecom.impl.Priceable._
import scala.collection.immutable._

/** A `BasicCartSystem` defines abstract members which are required
  * to build a cart.
  *
  * By implementing this interface, you can calculate the cart by
  * calling the `run` method. This is the recommended way to
  * use this library. Note that you can also extend the `CartSystem`,
  * which makes some assumptions on your cart's content.
  */
trait BasicCartSystem[T] {

  implicit val taxSystem: TaxSystem[T]
  implicit val mc: MathContext

  val priceMode: PriceMode.Value

  type CalcItem = CartItemCalculator[_, T]

  /** Define the calculation items to use
    */
  def buildCalculationItems: Seq[CalcItem]

  def run: CartResult[T] = {
    val items = buildCalculationItems
    Cart.fromItems[CalcItem, T](items, priceMode)
  }
}

/** Extends the BasicCartSystem for the common case of carts that include `LineCalc` items
  */
trait CartSystem[T] extends BasicCartSystem[T] {
  // The prices are not fetched directly from the articles, but are using a price service,
  // which may apply additional rules to the basic prices.
  // (we use the default implementation)
  val priceService = PriceService.DefaultPriceService

  // rounding strategy used to round line totals
  def lineRounding: Rounding = Rounding.defaultRounding

  def buildCartLines: Seq[Line[T]]

  def buildAdjustmentItems: Seq[CalcItem] = Seq.empty

  override def buildCalculationItems: Seq[CalcItem] = {
    implicit val r = lineRounding
    val lines = buildCartLines map { l: Line[T] => new LineCalc(l, priceService) }
    lines ++ buildAdjustmentItems
  }

}
