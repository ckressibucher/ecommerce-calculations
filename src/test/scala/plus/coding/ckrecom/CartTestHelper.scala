package plus.coding.ckrecom

import java.math.BigDecimal

import plus.coding.ckrecom.impl._

import TaxSystem._

/** Some utility values, methods and classes to test cart logic */
trait CartTestHelper {

  val taxCls10Pct: TaxSystem.DefaultTaxClass = TaxSystem.SimpleTax(10, 100)
  val taxFree: TaxSystem.DefaultTaxClass = TaxSystem.FreeTax

  val taxSystemDefault: TaxSystem[DefaultTaxClass] = DefaultTaxSystem

  val defaultProductImpl = new Product[DefaultTaxClass, SimpleProduct[DefaultTaxClass]] {
    def netPrice(product: SimpleProduct[DefaultTaxClass], qty: BigDecimal): Option[BigDecimal] =
      Some(product.price)

    def taxClass(product: SimpleProduct[DefaultTaxClass]): DefaultTaxClass =
      product.taxClass
  }

  def bigDec(value: String): BigDecimal = new BigDecimal(value)

  /** The most basic implementation of a Product */
  case class SimpleProduct[T: TaxSystem](price: BigDecimal, taxClass: T)

  object SimpleProduct {
    def apply[T: TaxSystem](price: String, tc: T): SimpleProduct[T] = {
      SimpleProduct(new BigDecimal(price), tc)
    }
  }

}
