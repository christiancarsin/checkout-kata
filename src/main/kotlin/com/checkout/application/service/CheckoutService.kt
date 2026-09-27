package com.checkout.application.service

import com.checkout.domain.model.Cart
import com.checkout.domain.model.Checkout
import com.checkout.domain.model.Money
import com.checkout.domain.model.PricingRule
import com.checkout.domain.model.Result
import com.checkout.domain.model.Sku
import com.checkout.domain.port.PricingRuleRepository

class CheckoutService(private val pricingRuleRepository: PricingRuleRepository) {

    fun startCheckout(pricingRules: List<PricingRule>? = null): CheckoutSession {
        val rules = pricingRules ?: pricingRuleRepository.findAll()
        return CheckoutSession(Checkout.create(rules))
    }

    class CheckoutSession internal constructor(private val checkout: Checkout) {

        val cart: Cart
            get() = checkout.cart

        fun scan(sku: Sku): Result<CheckoutSession> = checkout.scan(sku).map { CheckoutSession(it) }

        fun scan(skuChar: Char): Result<CheckoutSession> = checkout.scan(skuChar).map { CheckoutSession(it) }

        fun total(): Result<Money> = checkout.total()
    }
}
