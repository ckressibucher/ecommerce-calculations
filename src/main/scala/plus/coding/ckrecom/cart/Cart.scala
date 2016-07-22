package plus.coding.ckrecom.cart

import plus.coding.ckrecom._
import plus.coding.ckrecom.Tax.TaxClass
import javax.money.CurrencyUnit
import java.math.{BigDecimal, MathContext}
import scala.collection.immutable.Seq
import scala.util.{Try, Success, Failure}

object Cart {

  import plus.coding.ckrecom.Implicits.mathContext

  /** Processes the cart with the given calculater and checks for errors.
   *  
   *  The given calculator is supposed to calculate the final prices for
   *  all cart items. It is typically composed of multiple sub calculators
   *  which are responsible for a specific type of cart item.
   *  
   *  If the resulting list is empty, the cart is valid (with the given calculator)
   */
  def validate(cart: Cart, calculator: CartCalculator): Seq[Throwable] = {
    val finalCart = calculator(cart)
    cart.contents.foldRight(Seq.empty[Throwable]) {(i: CartContentItem, errs: Seq[Throwable]) => {
      i.finalPrices match {
        case Success(_) => errs
        case Failure(e) => e +: errs
      }
    }}
  }

}

/** The main cart class
  *
  */
case class Cart(
  val contents: Seq[CartContentItem],
  val currency: CurrencyUnit,
  val mode: PriceMode
)(implicit val mc: MathContext) {

  // mainly for debugging, as we don't have
  // "real" renderers yet.
  // TODO
  //override def toString = {
  //}
  
  /** Adds an item to the cart.
   *  
   *  This does not validate the content. You should call
   *  validate the final cart instance whenever you
   *  update a cart.
   */
  def addContent(item: CartContentItem): Cart = {
    new Cart(contents :+ item, currency, mode)
  }
  
  def addPriceable(p: Priceable): Cart = {
    // TODO more specific exception...
    val prices: Try[Seq[TaxedPrice]] = Failure(new RuntimeException("not yet calculated"))
    addContent(CartContentItem(p, prices))
  }
  
  /** Calculates the grand total of the current cart state.
    *
    * Only already calculated contents are summed up.
    */
  def grandTotal(): BigDecimal = {
    def sum(xs: Seq[BigDecimal]): BigDecimal = {
      xs.foldLeft(BigDecimal.ZERO)(_.add(_, mc))
    }
    val itemSums = for {
      item <- contents
      prices = item.finalPrices match {
        case Failure(_) => Seq.empty
        case Success(ps) => ps
      }
      priceAmnts = prices.map(_._1)
    } yield sum(priceAmnts)
    sum(itemSums)
  }

  def taxes(): BigDecimal = ???

  def valueByTaxClass: Map[TaxClass, (TaxRate, BigDecimal)] = ???
}
