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

    fun calculateTotal(pricingRules: Map<Sku, PricingRule>): Result<Money> {
        var total = Money.ZERO
        val processedSkus = mutableSetOf<Sku>()

        for ((sku, qty) in items) {
            if (sku in processedSkus) continue

            val rule = pricingRules[sku]
                ?: return Result.Failure(DomainError.UnknownSku(sku))

            val partnerQty = if (rule.promotion is Promotion.MealDeal) {
                val partnerSku = (rule.promotion as Promotion.MealDeal).partnerSku
                items.getOrDefault(partnerSku, 0)
            } else 0

            total = total.plus(rule.calculatePrice(qty, partnerQty))
            processedSkus.add(sku)
            if (partnerQty > 0) {
                val partnerSku = (rule.promotion as Promotion.MealDeal).partnerSku
                processedSkus.add(partnerSku)
            }
        }

        return Result.Success(total)
    }
}