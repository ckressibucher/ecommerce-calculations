package plus.coding.ckrecom.cart

import plus.coding.ckrecom._
import plus.coding.ckrecom.Tax.TaxClass
import javax.money.CurrencyUnit
import java.math.{BigDecimal, MathContext}
import scala.collection.immutable.Seq
import scala.util.{Try, Success, Failure}
import scala.util.Either


object Cart {

  import plus.coding.ckrecom.Implicits.mathContext

  type ErrString = String
  
  /** Processes the cart with the given calculater and checks for errors.
   *  
   *  The given calculator is supposed to calculate the final prices for
   *  all cart items. It is typically composed of multiple sub calculators
   *  which are responsible for a specific type of cart item.
   *  
   *  If the resulting list is empty, the cart is valid (with the given calculator)
   */
  def validate(cart: Cart[_], calculator: CartCalculator): Seq[ErrString] = {
    val finalCart = calculator(cart)
    cart.contents.foldRight(Seq.empty[ErrString]) {(i: CartContentItem, errs: Seq[ErrString]) => {
      i.finalPrices match {
        case Right(_) => errs
        case Left(e) => e +: errs
      }
    }}
  }

}

/** The main cart class
  *
  */
class Cart[M <: PRICE_MODE](
  val contents: Seq[CartContentItem],
  val currency: CurrencyUnit
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
  def addContent(item: CartContentItem): Cart[M] = {
    new Cart(contents :+ item, currency)
  }
  
  def addPriceable(p: Priceable): Cart[M] = {
    val item = CartContentItem(p, Left("not yet calculated"))
    addContent(item)
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
        case Left(_) => Seq.empty
        case Right(ps) => ps
      }
      priceAmnts = prices.map(_._1)
    } yield sum(priceAmnts)
    sum(itemSums)
  }

  def taxes(): BigDecimal = ???

  def valueByTaxClass: Map[TaxClass, (TaxRate, BigDecimal)] = ???
}
