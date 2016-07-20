package plus.coding.ckrecom.cart

import plus.coding.ckrecom._
import javax.money.CurrencyUnit
import java.math.BigDecimal
import scala.collection.immutable.Seq
import Tax.TaxClass

object Cart {

  implicit val defaultPriceService: PriceService = new DefaultPriceService
}

/** The main cart class
  *
  */
class Cart[M <: PRICE_MODE](
  val contents: Seq[CartContentItem],
  val currency: CurrencyUnit
) {

  implicit val mc: java.math.MathContext
  
  // mainly for debugging, as we don't have
  // "real" renderers yet.
  // TODO
  //override def toString = {
  //}
  
  def addContent(item: CartContentItem): Either[String, Cart[M]] = {
    if (item._1.canCurrency(currency))
      Right(new Cart(contents :+ item, currency))
    else
      Left(s"Currency $currency not supported by cart item")
  }
  
  /** Calculates the grand total of the currenct cart state.
    *
    * Only already calculated contents are summed up.
    */
  def grandTotal(): BigDecimal = {
    def sum(xs: Seq[BigDecimal]): BigDecimal = {
      xs.foldLeft(BigDecimal.ZERO)(_.add(_, mc))
    }
    val itemSums = for {
      item <- contents
      itemPrices = item._2
      itemSum = sum(itemPrices.map(_._1))
    } yield itemSum
    sum(itemSums)
  }

  def taxes(): BigDecimal = ???

  def valueByTaxClass: Map[TaxClass, (TaxRate, BigDecimal)] = ???
}
