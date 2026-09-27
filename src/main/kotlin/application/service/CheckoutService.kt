package com.checkout.application.service

import com.checkout.domain.model.Checkout
import com.checkout.domain.model.Money
import com.checkout.domain.model.PricingRule
import com.checkout.domain.model.Sku

class CheckoutService(private val pricingRules: List<PricingRule> = Checkout.defaultPricingRules()) {

    fun startCheckout(): CheckoutSession {
        return CheckoutSession(Checkout.create(pricingRules))
    }

    data class CheckoutSession(private val checkout: Checkout) {

        fun scan(sku: Sku): CheckoutSession {
            checkout.scan(sku)
            return this
        }

        fun scan(skuChar: Char): CheckoutSession {
            checkout.scan(skuChar)
            return this
        }

        fun scan(skuString: String): CheckoutSession {
            checkout.scan(skuString)
            return this
        }

        fun total(): Money {
            return checkout.total()
        }

        fun getCart() = checkout.getCart()
    }
}