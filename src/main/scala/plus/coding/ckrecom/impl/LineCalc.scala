package plus.coding.ckrecom
package impl

import java.math.{BigDecimal, MathContext}

import plus.coding.ckrecom.Product.ProductOps
import plus.coding.ckrecom.impl.Priceable._

import scala.collection.immutable._

/** Calculate the final prices for an item line using a [[Product]] implementation
  */
class LineCalc[T: TaxSystem, P](val line: Line[T, P])
                               (implicit val rounding: Rounding, val pev: Product[T, P], val mc: MathContext)
  extends CartItemCalculator[Line[T, P], T] {

  val priceable = line

  val taxSystem = implicitly[TaxSystem[T]]

  def optionPriceToEither(price: Option[BigDecimal]): Either[String, BigDecimal] = price match {
    case Some(p) => Right(p)
    case None => Left("The product has no price")
  }

  def finalPrices(c: CartBase[T]): PriceResult[T] = {
    val unitPrice: Either[String, BigDecimal] = c.mode match {
      case PriceMode.PRICE_NET =>
        optionPriceToEither( line.product.netPrice(line.qty) )
      case PriceMode.PRICE_GROSS =>
        optionPriceToEither( line.product.grossPrice(line.qty))
    }

    unitPrice.right map { p: BigDecimal =>
      // price for the whole line
      val linePrice = p.multiply(line.qty)
      // round the price and wrap it into the required PriceResult structure
      Map(line.product.taxClass -> rounding(linePrice))
    }
  }
}
