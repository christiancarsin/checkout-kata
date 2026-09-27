package com.checkout.application.service

import com.checkout.domain.model.Checkout
import com.checkout.domain.model.DomainError
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

    data class CheckoutSession(private val checkout: Checkout) {

        fun scan(sku: Sku): Result<CheckoutSession> {
            return checkout.scan(sku).map { this }
        }

        fun scan(skuChar: Char): Result<CheckoutSession> {
            return checkout.scan(skuChar).map { this }
        }

        fun scan(skuString: String): Result<CheckoutSession> {
            return checkout.scan(skuString).map { this }
        }

        fun total(): Result<Money> {
            return checkout.total()
        }

        fun getCart() = checkout.getCart()
    }
}