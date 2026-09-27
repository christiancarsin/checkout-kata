package com.checkout.domain.model

data class Cart(private val items: Map<Sku, Int> = emptyMap()) {

    fun addItem(sku: Sku, quantity: Int = 1): Cart {
        require(quantity > 0) { "Quantity must be positive" }
        val newQuantity = items.getOrDefault(sku, 0) + quantity
        return Cart(items + (sku to newQuantity))
    }

    fun getQuantity(sku: Sku): Int = items.getOrDefault(sku, 0)

    fun getItems(): Map<Sku, Int> = items

    fun isEmpty(): Boolean = items.isEmpty()

    fun totalItems(): Int = items.values.sum()

    fun calculateTotal(pricingRules: Map<Sku, PricingRule>): Money {
        var total = Money.ZERO
        val processedSkus = mutableSetOf<Sku>()

        for ((sku, qty) in items) {
            if (sku in processedSkus) continue

            val rule = pricingRules[sku] ?: continue
            val promotion = rule.promotion

            when (promotion) {
                is Promotion.MealDeal -> {
                    val partnerSku = promotion.partnerSku
                    val partnerQty = items.getOrDefault(partnerSku, 0)
                    total = total.plus(rule.calculatePrice(qty, partnerQty))
                    processedSkus.add(sku)
                    processedSkus.add(partnerSku)
                }
                else -> {
                    total = total.plus(rule.calculatePrice(qty))
                    processedSkus.add(sku)
                }
            }
        }

        return total
    }
}