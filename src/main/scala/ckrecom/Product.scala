package ckrecom

import javax.money._

trait Product {

  type ProductId

  val id: ProductId

  /** Returns the base net price for this article.
   */
  def netPrice: MonetaryAmount

  def taxClass: TaxClass
}

case class SimpleProduct(
  id: String,
  netPrice: MonetaryAmount,
  taxClass: TaxClass,
  name: String) extends Product {

  type ProductId = String
}


