package com.checkout.domain.model

data class PricingRule(
    val sku: Sku,
    val unitPrice: Money,
    val promotion: Promotion = Promotion.None
) {

    fun priceLine(quantity: Int): Money = promotion.priceLine(quantity, unitPrice)
}
