package plus.coding.ckrecom.cart

import javax.money.CurrencyUnit
import plus.coding.ckrecom.PriceMode
import plus.coding.ckrecom.tax.TaxSystem
import scala.collection.immutable.Seq
import java.math.MathContext

import plus.coding.ckrecom.cart.calc._
import plus.coding.ckrecom.cart.Priceable.Line

object CartApi {

  /** Implement this to define your specific cart building context
    */
  trait BasicCartSystem[T] {

    implicit val taxSystem: TaxSystem[T]
    implicit val mc: MathContext

    // The cart must define one single currency
    val currency: CurrencyUnit

    val priceMode: PriceMode.Value

    type CalcItem = CartItemPre[_, T]

    /** Define the calculation items to use
      */
    def buildCalculationItems: Seq[CalcItem]

    def run: CartResult[T] = {
      val items = buildCalculationItems
      Cart.fromItems[CalcItem, T](items, currency, priceMode)
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
}
