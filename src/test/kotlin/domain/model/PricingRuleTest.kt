package com.checkout.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PricingRuleTest {

    @Test
    fun `create pricing rule with unit price only`() {
        val rule = PricingRule(Sku.of('A'), Money.pence(50))

        assertThat(rule.sku.value).isEqualTo('A')
        assertThat(rule.unitPrice.toPence()).isEqualTo(50)
        assertThat(rule.promotion).isInstanceOf(Promotion.None::class.java)
    }

    @Test
    fun `create pricing rule with multi-price promotion`() {
        val rule = PricingRule(Sku.of('A'), Money.pence(50), Promotion.MultiPrice(3, Money.pence(130)))

        assertThat(rule.promotion).isInstanceOf(Promotion.MultiPrice::class.java)
        val promo = rule.promotion as Promotion.MultiPrice
        assertThat(promo.quantity).isEqualTo(3)
        assertThat(promo.price.toPence()).isEqualTo(130)
    }

    @Test
    fun `create pricing rule with buy n get one free promotion`() {
        val rule = PricingRule(Sku.of('C'), Money.pence(25), Promotion.BuyNGetOneFree(3))

        assertThat(rule.promotion).isInstanceOf(Promotion.BuyNGetOneFree::class.java)
        val promo = rule.promotion as Promotion.BuyNGetOneFree
        assertThat(promo.buyQuantity).isEqualTo(3)
    }

    @Test
    fun `create pricing rule with meal deal promotion`() {
        val rule = PricingRule(Sku.of('D'), Money.pence(150), Promotion.MealDeal(Sku.of('E'), Money.pence(300)))

        assertThat(rule.promotion).isInstanceOf(Promotion.MealDeal::class.java)
        val promo = rule.promotion as Promotion.MealDeal
        assertThat(promo.partnerSku.value).isEqualTo('E')
        assertThat(promo.dealPrice.toPence()).isEqualTo(300)
    }

    @Test
    fun `pricing rule equality`() {
        val rule1 = PricingRule(Sku.of('A'), Money.pence(50), Promotion.MultiPrice(3, Money.pence(130)))
        val rule2 = PricingRule(Sku.of('A'), Money.pence(50), Promotion.MultiPrice(3, Money.pence(130)))
        val rule3 = PricingRule(Sku.of('B'), Money.pence(75), Promotion.MultiPrice(2, Money.pence(125)))

        assertThat(rule1).isEqualTo(rule2)
        assertThat(rule1).isNotEqualTo(rule3)
    }

    @Test
    fun `calculate price with no promotion`() {
        val rule = PricingRule(Sku.of('A'), Money.pence(50))

        val price = rule.calculatePrice(3)

        assertThat(price.toPence()).isEqualTo(150)
    }

    @Test
    fun `calculate price with multi-price promotion`() {
        val rule = PricingRule(Sku.of('A'), Money.pence(50), Promotion.MultiPrice(3, Money.pence(130)))

        val price = rule.calculatePrice(3)

        assertThat(price.toPence()).isEqualTo(130)
    }

    @Test
    fun `calculate price with meal deal promotion`() {
        val rule = PricingRule(Sku.of('D'), Money.pence(150), Promotion.MealDeal(Sku.of('E'), Money.pence(300)))

        val price = rule.calculatePrice(2, 2)

        assertThat(price.toPence()).isEqualTo(600)
    }
}