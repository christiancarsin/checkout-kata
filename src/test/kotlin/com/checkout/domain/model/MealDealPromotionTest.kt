package com.checkout.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MealDealPromotionTest {

    private val unitPriceD = Money.pence(150)
    private val unitPriceE = Money.pence(200)
    private val promoD = Promotion.MealDeal(Sku.of('E'), Money.pence(300))
    private val promoE = Promotion.MealDeal(Sku.of('D'), Money.pence(300))

    @Test
    fun `complete pair is charged the bundle price`() {
        assertThat(promoD.priceBundle(1, 1).toPence()).isEqualTo(300)
        assertThat(promoE.priceBundle(1, 1).toPence()).isEqualTo(300)
    }

    @Test
    fun `bundle price is the same whichever side declares the deal`() {
        assertThat(promoD.priceBundle(2, 1)).isEqualTo(promoE.priceBundle(2, 1))
    }

    @Test
    fun `no bundle is charged when the partner is missing`() {
        assertThat(promoD.priceBundle(1, 0).toPence()).isZero()
        assertThat(promoD.priceLine(1, unitPriceD).toPence()).isEqualTo(150)
    }

    @Test
    fun `bundle price is applied once per pair`() {
        val ruleD = PricingRule(Sku.of('D'), unitPriceD, promoD)

        assertThat(ruleD.priceLine(2).toPence()).isEqualTo(300)
        assertThat(promoD.priceBundle(2, 2).toPence()).isEqualTo(600)
    }

    @Test
    fun `items beyond the bundle are charged at unit price`() {
        val ruleD = PricingRule(Sku.of('D'), unitPriceD, promoD)

        val price = promoD.priceBundle(3, 2).plus(ruleD.priceLine(1))

        assertThat(price.toPence()).isEqualTo(750)
    }

    @Test
    fun `bundle price uses the smaller of the two quantities`() {
        assertThat(promoD.priceBundle(5, 2).toPence()).isEqualTo(600)
        assertThat(promoD.priceBundle(0, 0).toPence()).isZero()
    }

    @Test
    fun `leftover partner items are charged at their own unit price`() {
        val ruleE = PricingRule(Sku.of('E'), unitPriceE, promoE)

        val price = promoE.priceBundle(2, 1).plus(ruleE.priceLine(1))

        assertThat(price.toPence()).isEqualTo(500)
    }
}
