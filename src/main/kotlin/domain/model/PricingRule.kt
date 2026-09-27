package com.checkout.domain.model

data class PricingRule(
    val sku: Sku,
    val unitPrice: Money,
    val promotion: Promotion = Promotion.None
) {

    fun calculatePrice(quantity: Int, partnerQuantity: Int = 0): Money {
        return promotion.calculatePrice(quantity, unitPrice, partnerQuantity)
    }
}