package com.checkout.domain.model

data class PricingRule(
    val sku: Sku,
    val unitPrice: Money,
    val promotion: Promotion = Promotion.None
) {

    fun calculatePrice(quantity: Int): Money {
        return promotion.calculatePrice(quantity, unitPrice)
    }

    fun calculatePrice(quantity: Int, partnerQuantity: Int): Money {
        return when (promotion) {
            is Promotion.MealDeal -> {
                val pairs = minOf(quantity, partnerQuantity)
                val remainder = quantity - pairs
                promotion.dealPrice.times(pairs).plus(unitPrice.times(remainder))
            }
            else -> calculatePrice(quantity)
        }
    }
}