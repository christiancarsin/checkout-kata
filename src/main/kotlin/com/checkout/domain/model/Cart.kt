package com.checkout.domain.model

data class Cart(private val contents: Map<Sku, Int> = emptyMap()) {

    init {
        require(contents.values.all { it > 0 }) { "Cart quantities must be positive" }
    }

    val items: Map<Sku, Int> get() = contents.toMutableMap()

    val isEmpty: Boolean get() = contents.isEmpty()

    val totalItems: Int get() = contents.values.sum()

    operator fun get(sku: Sku): Int = contents.getOrDefault(sku, 0)

    fun addItem(sku: Sku, quantity: Int = 1): Result<Cart> {
        if (quantity <= 0) {
            return Result.Failure(DomainError.InvalidQuantity(quantity))
        }
        return Result.Success(Cart(contents + (sku to (this[sku] + quantity))))
    }

    fun calculateTotal(pricingRules: Map<Sku, PricingRule>): Result<Money> {
        for (sku in contents.keys) {
            if (sku !in pricingRules) {
                return Result.Failure(DomainError.UnknownSku(sku))
            }
        }

        val remaining = contents.toMutableMap()
        val appliedBundles = mutableSetOf<Set<Sku>>()
        var total = Money.ZERO

        for (rule in pricingRules.values) {
            val mealDeal = rule.promotion as? Promotion.MealDeal
            if (mealDeal != null && appliedBundles.add(setOf(rule.sku, mealDeal.partnerSku))) {
                total = total.plus(bundlePrice(rule, mealDeal, remaining))
            }
        }

        for ((sku, quantity) in remaining) {
            if (quantity > 0) {
                total = total.plus(pricingRules.getValue(sku).priceLine(quantity))
            }
        }

        return Result.Success(total)
    }

    private fun bundlePrice(
        rule: PricingRule,
        mealDeal: Promotion.MealDeal,
        remaining: MutableMap<Sku, Int>
    ): Money {
        val quantity = remaining.getOrDefault(rule.sku, 0)
        val partnerQuantity = remaining.getOrDefault(mealDeal.partnerSku, 0)
        val bundled = minOf(quantity, partnerQuantity)
        if (bundled == 0) {
            return Money.ZERO
        }

        remaining[rule.sku] = quantity - bundled
        remaining[mealDeal.partnerSku] = partnerQuantity - bundled
        return mealDeal.priceBundle(quantity, partnerQuantity)
    }
}
