package com.checkout.domain.port

import com.checkout.domain.model.PricingRule

interface PricingRuleRepository {
    fun findAll(): List<PricingRule>
}