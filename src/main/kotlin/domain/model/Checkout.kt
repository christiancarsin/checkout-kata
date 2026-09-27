package com.checkout.domain.model

class Checkout(private val pricingRules: Map<Sku, PricingRule>) {

    private var cart: Cart = Cart()

    fun scan(sku: Sku): Result<Checkout> {
        return scan(sku, 1)
    }

    fun scan(sku: Sku, quantity: Int): Result<Checkout> {
        if (quantity <= 0) {
            return Result.Failure(DomainError.InvalidQuantity(quantity))
        }
        if (sku !in pricingRules) {
            return Result.Failure(DomainError.UnknownSku(sku))
        }
        return cart.addItem(sku, quantity).flatMap { newCart ->
            cart = newCart
            Result.Success(this)
        }
    }

    fun scan(skuChar: Char): Result<Checkout> {
        return scan(Sku.of(skuChar))
    }

    fun scan(skuString: String): Result<Checkout> {
        return scan(Sku.of(skuString))
    }

    fun total(): Result<Money> {
        return cart.calculateTotal(pricingRules)
    }

    fun getCart(): Cart = cart

    companion object {
        fun create(pricingRules: List<PricingRule>): Checkout {
            val rulesMap = pricingRules.associateBy { it.sku }
            return Checkout(rulesMap)
        }
    }
}