Cart calculations
=======


A shopping cart contains:

* cart lines, each with product (with tax class) and quantity
* adjustments (e.g. discount or shipping fee, *no taxes!*), they also need
  to defined their tax class

For calculations, we need additionally:

* context to get final price for each product. This may be arbitrary data
  like customer numbers etc., and is handled by a price service.
* tax system which knows the tax rate for each tax class
* whether or not to apply taxes to calculate the grand total.

When an order is finalised, the following data needs to be fixed/persisted:
(this data is calculated dynamically for temporary carts, see the list before)

* whether or not to apply taxes
* final price for each cart line
* which tax rate (not only class) is applied to each cart item / adjustment

Based on this data, the customer or merchant needs to see:

* A list of cart lines, each containing:
  - a quantity
  - some way to identify the product (e.g. sku)
  - the price per one item
  - the price per line
  - the tax rate associated with the product
  - whether the price includes the tax or not
    (note: we do NOT want to show the exact amount of tax,
    neither the net and gross amount, per line. Taxes are only
    calculated for the sum of all lines/adjustments of each tax rate.
    This prevents inconsistencies due to rounding).
* A list of adjustments, each containing:
  - a label/description
  - the value (monetary amount)
  - the tax rate
  - whether the value includes the tax or not
* The grand total, either as net amount or gross amount, depending
  on the current customer's context. It must be declared somehow,
  what the value means (net or gross price).
* If grand total is "gross", then show also the "net grand total"
* The tax amount for each tax rate (applied to the *sum* of all
  lines/adjustments with the given rate).
* The total tax amount (sum of taxes per rate).
