package com.checkout.domain.model

class Checkout private constructor(
    private val pricingRules: Map<Sku, PricingRule>,
    val cart: Cart = Cart()
) {

    fun scan(sku: Sku, quantity: Int = 1): Result<Checkout> {
        if (sku !in pricingRules) {
            return Result.Failure(DomainError.UnknownSku(sku))
        }
        return cart.addItem(sku, quantity).map { Checkout(pricingRules, it) }
    }

    fun scan(skuChar: Char): Result<Checkout> = scan(skuChar.toString())

    fun scan(skuString: String): Result<Checkout> = skuOf(skuString).flatMap { scan(it) }

    fun total(): Result<Money> = cart.calculateTotal(pricingRules)

    companion object {
        fun create(pricingRules: List<PricingRule>): Checkout {
            require(pricingRules.distinctBy { it.sku }.size == pricingRules.size) {
                "Duplicate SKU in pricing rules: ${
                    pricingRules.groupBy { it.sku }.filterValues { it.size > 1 }.keys
                }"
            }
            return Checkout(pricingRules.associateBy { it.sku })
        }

        private fun skuOf(raw: String): Result<Sku> = try {
            Result.Success(Sku.of(raw))
        } catch (e: IllegalArgumentException) {
            Result.Failure(DomainError.InvalidSku(raw, e.message.orEmpty()))
        }
    }
}
