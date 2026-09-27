package com.checkout.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MealDealPromotionTest {

    private val unitPriceD = Money.pence(150)
    private val unitPriceE = Money.pence(200)
    private val promoD = Promotion.MealDeal(Sku.of('E'), Money.pence(300))
    private val promoE = Promotion.MealDeal(Sku.of('D'), Money.pence(300))

    @Test
    fun `calculate price when both items present for meal deal`() {
        val priceD = promoD.calculatePrice(1, unitPriceD, 1)
        val priceE = promoE.calculatePrice(1, unitPriceE, 1)

        assertThat(priceD.toPence()).isEqualTo(300)
        assertThat(priceE.toPence()).isEqualTo(300)
    }

    @Test
    fun `calculate price when only one meal deal item present`() {
        val price = promoD.calculatePrice(1, unitPriceD, 0)

        assertThat(price.toPence()).isEqualTo(150)
    }

    @Test
    fun `calculate price for multiple meal deal pairs`() {
        val ruleD = PricingRule(Sku.of('D'), unitPriceD, promoD)
        val price = ruleD.calculatePrice(2, 2)

        assertThat(price.toPence()).isEqualTo(600)
    }

    @Test
    fun `calculate price with extra items beyond meal deal pairs`() {
        val ruleD = PricingRule(Sku.of('D'), unitPriceD, promoD)
        val price = ruleD.calculatePrice(3, 2)

        assertThat(price.toPence()).isEqualTo(750)
    }

    @Test
    fun `meal deal applies regardless of scan order`() {
        val ruleD = PricingRule(Sku.of('D'), unitPriceD, promoD)
        val ruleE = PricingRule(Sku.of('E'), unitPriceE, promoE)

        val priceDFirst = ruleD.calculatePrice(1, 1)
        val priceEFirst = ruleE.calculatePrice(1, 1)

        assertThat(priceDFirst.toPence()).isEqualTo(300)
        assertThat(priceEFirst.toPence()).isEqualTo(300)
    }

    @Test
    fun `meal deal with unequal quantities uses min`() {
        val ruleD = PricingRule(Sku.of('D'), unitPriceD, promoD)

        val price = ruleD.calculatePrice(5, 2)

        assertThat(price.toPence()).isEqualTo(1050)
    }
}