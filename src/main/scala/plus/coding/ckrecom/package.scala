package plus.coding

package object ckrecom {
  sealed trait PRICE_MODE
  case object PRICE_NET extends PRICE_MODE
  case object PRICE_GROSS extends PRICE_MODE
  
  trait WithMathContext {
    import Implicits.mathContext
  }
}