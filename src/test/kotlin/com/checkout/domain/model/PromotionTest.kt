package com.checkout.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PromotionTest {

    private val unitPrice = Money.pence(25)
    private val promo = Promotion.BuyNGetOneFree(3)

    @Test
    fun `calculate price when quantity qualifies for free item`() {
        val price = promo.priceLine(4, unitPrice)

        assertThat(price.toPence()).isEqualTo(75)
    }

    @Test
    fun `calculate price when quantity does not qualify for free item`() {
        val price = promo.priceLine(2, unitPrice)

        assertThat(price.toPence()).isEqualTo(50)
    }

    @Test
    fun `calculate price for multiple free items`() {
        val price = promo.priceLine(8, unitPrice)

        assertThat(price.toPence()).isEqualTo(150)
    }

    @Test
    fun `calculate price at exact threshold`() {
        val price = promo.priceLine(3, unitPrice)

        assertThat(price.toPence()).isEqualTo(75)
    }

    @Test
    fun `calculate price just above threshold`() {
        val price = promo.priceLine(5, unitPrice)

        assertThat(price.toPence()).isEqualTo(100)
    }

    @Test
    fun `calculate price for zero quantity`() {
        val price = promo.priceLine(0, unitPrice)

        assertThat(price.toPence()).isZero()
    }

    @Test
    fun `meal deal bundle price for a complete pair`() {
        val mealDeal = Promotion.MealDeal(Sku.of('E'), Money.pence(300))

        assertThat(mealDeal.priceBundle(1, 1).toPence()).isEqualTo(300)
    }

    @Test
    fun `meal deal bundle price for extra items`() {
        val mealDeal = Promotion.MealDeal(Sku.of('E'), Money.pence(300))

        val bundled = mealDeal.priceBundle(2, 1)
        val leftovers = mealDeal.priceLine(1, Money.pence(150))

        assertThat(bundled.plus(leftovers).toPence()).isEqualTo(450)
    }

    @Test
    fun `meal deal charges nothing without a partner`() {
        val mealDeal = Promotion.MealDeal(Sku.of('E'), Money.pence(300))

        assertThat(mealDeal.priceBundle(2, 0).toPence()).isZero()
        assertThat(mealDeal.priceLine(2, Money.pence(150)).toPence()).isEqualTo(300)
    }

    @Test
    fun `buy n get one free requires at least one item to buy`() {
        assertThatThrownBy { Promotion.BuyNGetOneFree(0) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("at least 1")
    }

    @Test
    fun `multi price requires a positive group size`() {
        assertThatThrownBy { Promotion.MultiPrice(0, Money.pence(100)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("positive")
    }
}
