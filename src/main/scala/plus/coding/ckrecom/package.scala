package plus.coding

package object ckrecom {

  // enum for price mode: can be either a PRICE_NET or a PRICE_GROSS
  sealed trait PriceMode
  case object PRICE_NET extends PriceMode
  case object PRICE_GROSS extends PriceMode

  trait WithMathContext {
    import Implicits.mathContext
  }
}