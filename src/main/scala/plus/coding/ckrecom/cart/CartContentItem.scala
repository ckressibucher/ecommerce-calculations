package plus.coding.ckrecom.cart

import scala.collection.immutable.Seq
import scala.util.Try

/**
 * Used to define the contents of a cart.
 *
 * Can be something like a cart line of product and quantity,
 * a shipping fee, or a discount; together with a list of
 * calculated prices resulting from this thing.
 */
case class CartContentItem(val p: Priceable, finalPrices: Try[Seq[TaxedPrice]])