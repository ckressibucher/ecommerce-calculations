package plus.coding.ckrecom

import scala.math.Numeric
import java.math.BigDecimal
import Tax.TaxClass
import scala.util.Try

package object cart {

  /** A price used in the cart.
    *
    * The cart has a defined currency,
    * so we don't need one in the price.
    * But we need a TaxClass which should be
    * used to calculate the net/gross amount
    * (the given numeric value represents the
    * type defined for the cart's mode).
    *
    */
  type TaxedPrice = (BigDecimal, TaxClass)

}
