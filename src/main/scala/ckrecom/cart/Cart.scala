package ckrecom.cart

import ckrecom._
import javax.money.{CurrencyUnit,MonetaryAmount}
import org.javamoney.moneta.Money

object Cart {

  implicit val defaultPriceService: PriceService = new DefaultPriceService
}

/** The main cart class
 *
 *  @param lines The cart lines/items
 *  @param adjustments Adjustments (e.g. discount, fee..)
 *  @param priceMode Whether lines and adjustments define net or gross prices
  */
class Cart[T : Numeric, M <: PRICE_MODE, C <: CurrencyUnit](
  val contents: Seq[CartContentItem[T]]
) {

  // mainly for debugging, as we don't have
  // "real" renderers yet.
  // TODO
  //override def toString = {
  //}
  
  /** Calculates the grand total of the currenct cart state.
    *
    * Only already calculated contents are summed up.
    */
  def grandTotal(): T = {
    val itemSums = for {
      item <- contents
      itemPrices = item._2
      itemSum = itemPrices.map(_._1).sum
    } yield itemSum
    itemSums.sum
  }

  def taxes(): MonetaryAmount = ???

  def valueByTaxClass: Map[TaxClass, (TaxRate, MonetaryAmount)] = ???
}
