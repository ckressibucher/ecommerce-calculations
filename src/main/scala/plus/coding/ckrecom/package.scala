package plus.coding

package object ckrecom {

  object PriceMode extends Enumeration {
    val PRICE_NET, PRICE_GROSS = Value
  }

  trait WithMathContext {
    import Implicits.mathContext
  }
}