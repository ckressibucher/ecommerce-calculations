Shopping cart calculations library
==============

This library aims to help calculating prices and taxes in shopping carts (or orders, invoices, ...).

It provides a framework to build a cart from different "items"
(products, fees, discounts, ...), where the price calculations of one
item may depend on another item. Finally, the taxes are calculated
from the sum of all items *per tax class*.

For a more detailled description see the [project page](https://coding.plus/projects/ecommerce-calculations-library.html).

Status
----

Currently under development

Usage
----

Note: there are currenctly no artifacts deployed to a maven repository, so you need to clone the git repository to use it.

For a usage example, see the [demo script](src/test/scala/plus/coding/ckrecom/usage/UsageExample.scala).
This script can also be run, using `sbt test:run`.


The recommended way to use this library is to define a *cart system* class which extends `BasicCartSystem`:


    class MyCartSytem extends BasicCartSystem[MyTaxClass] {
      override implicit val taxSystem: TaxSystem[MyTaxClass] = ???
      override implicit val mc: MathContext = ???

      override val priceMode: PriceMode.Value = PriceMode.PRICE_GROSS

      /** Define the calculation items to use
        */
      override def buildCalculationItems: Seq[CalcItem] = ???
    }
    
    val result = new MyCartSystem.run

This trait declares all the members needed to build a cart. An example implementation can be
found in the test directory, in the `UsageExample` object.

