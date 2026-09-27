package com.checkout.adapter.repository

import com.checkout.domain.model.Money
import com.checkout.domain.model.PricingRule
import com.checkout.domain.model.Promotion
import com.checkout.domain.model.Sku
import com.checkout.domain.port.PricingRuleRepository

class InMemoryPricingRuleRepository : PricingRuleRepository {
    override fun findAll(): List<PricingRule> = listOf(
        PricingRule(Sku.of('A'), Money.pence(50), Promotion.MultiPrice(3, Money.pence(130))),
        PricingRule(Sku.of('B'), Money.pence(75), Promotion.MultiPrice(2, Money.pence(125))),
        PricingRule(Sku.of('C'), Money.pence(25), Promotion.BuyNGetOneFree(3)),
        PricingRule(Sku.of('D'), Money.pence(150), Promotion.MealDeal(Sku.of('E'), Money.pence(300))),
        PricingRule(Sku.of('E'), Money.pence(200), Promotion.MealDeal(Sku.of('D'), Money.pence(300)))
    )
}
