Data types
====

The main data types involved are:

* A `TaxRate`. Simply a rational number to calculate a tax for a given price value.
* `TaxSystem`. A `TaxSystem[C]` is a typeclass where the given type `C` represents
  a *tax class*. The `TaxSystem` trait defines one method `rate(c: C)`, which returns
  a `TaxRate` for the given tax class `C`.  
  It would have been possible to implement this behaviour with just a `trait TaxClass`,
  containg a method
  `rate`. However, a `TaxSystem` together with an unconstrained type parameter for tax classes
  does better express the idea of a *system, that defines a set of tax classes, and knows what
  they mean (i.e. what tax rates they represent)*  
  There is a `DefaultTaxClass` type with and a corresponding `TaxSystem` implementation, for
  basic needs.
* A `Product` defines the interface to get a price and the tax class of something that can be sold.
  It is a trait that can (and probably will) be implemented by the library user, in order to
  define prices for the specific types of products used in the application. This implementation is
  one of the main binding glues between the user's application and the library.  
* `Priceable`. A list of `Priceable`s define (most of) a cart. There are some default implementations:
    - `Line`: a `Product` and a quantity
    - `FixedDiscount`: Defines a discount by a fixed amount of money.
    - `PctDiscount`: A percentage based discount
    - `Shipping`: A shipping fee
    - `Fee`: An arbitrary kind of fee  
  The library user can define other priceables.
* `CartItemCalculator` This wraps a `Priceable`, and provides a method to calculate the *final prices*
  for that priceable. In general, there will be one `CartItemCalculator` type per `Priceable`, which knows
  how to calculate prices for it. To calculate the final prices, the previous `Cart` object is
  provided to the `CartItemCalculator`. This makes it possible to calculate things like percentage
  discounts, which are based on prices of previous items. But it does also require,
  that the `CartItemCalculator.finalPrices` are called in the
  correct order.
* A `Cart` is created from a list of `CartItemCalculator` objects. To do this, the factory `Cart.fromItems`
  does a `foldLeft`, starting with an empty cart, calculating the final prices for each item, and
  adds a `CartContentItem` to the current cart. A `CartContentItem` contains a `Priceable` and the
  calculated prices.
* `PriceResult` is the type of the *final prices* returned by the `CartItemCalculator.finalPrices` method.
  It is a type alias for an `Either[String, Map[T, Long]]`, so it can either contain an error string, or
  a map with tax class keys (type `T`) to `Long` values (price values, representing cents).
  The map (instead of just one `(T, Long)` tuple) is required to allow a priceable to be splitted into
  multiple tax classes. This maybe useful for example to calculate a discount for each product,
  and apply the product's tax class to the discount.  
  With all of this information, the `Cart` can calculate a grand total, and the sum of all taxes.

