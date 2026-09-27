package com.checkout.domain.model

class Checkout(private val pricingRules: Map<Sku, PricingRule>) {

    private var cart: Cart = Cart()

    fun scan(sku: Sku): Checkout {
        cart = cart.addItem(sku)
        return this
    }

    fun scan(skuChar: Char): Checkout {
        return scan(Sku.of(skuChar))
    }

    fun scan(skuString: String): Checkout {
        return scan(Sku.of(skuString))
    }

    fun total(): Money {
        return cart.calculateTotal(pricingRules)
    }

    fun getCart(): Cart = cart

    companion object {
        fun create(pricingRules: List<PricingRule>): Checkout {
            val rulesMap = pricingRules.associateBy { it.sku }
            return Checkout(rulesMap)
        }

        fun create(): Checkout {
            return create(defaultPricingRules())
        }

        public fun defaultPricingRules(): List<PricingRule> {
            return listOf(
                PricingRule(Sku.of('A'), Money.pence(50), Promotion.MultiPrice(3, Money.pence(130))),
                PricingRule(Sku.of('B'), Money.pence(75), Promotion.MultiPrice(2, Money.pence(125))),
                PricingRule(Sku.of('C'), Money.pence(25), Promotion.BuyNGetOneFree(3)),
                PricingRule(Sku.of('D'), Money.pence(150), Promotion.MealDeal(Sku.of('E'), Money.pence(300))),
                PricingRule(Sku.of('E'), Money.pence(200), Promotion.MealDeal(Sku.of('D'), Money.pence(300)))
            )
        }
    }
}