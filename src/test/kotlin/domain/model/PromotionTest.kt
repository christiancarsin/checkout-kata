package com.checkout.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PromotionTest {

    private val unitPrice = Money.pence(25)
    private val promo = Promotion.BuyNGetOneFree(3)

    @Test
    fun `calculate price when quantity qualifies for free item`() {
        val price = promo.calculatePrice(4, unitPrice)

        assertThat(price.toPence()).isEqualTo(75)
    }

    @Test
    fun `calculate price when quantity does not qualify for free item`() {
        val price = promo.calculatePrice(2, unitPrice)

        assertThat(price.toPence()).isEqualTo(50)
    }

    @Test
    fun `calculate price for multiple free items`() {
        val price = promo.calculatePrice(8, unitPrice)

        assertThat(price.toPence()).isEqualTo(150)
    }

    @Test
    fun `calculate price at exact threshold`() {
        val price = promo.calculatePrice(3, unitPrice)

        assertThat(price.toPence()).isEqualTo(75)
    }

    @Test
    fun `calculate price just above threshold`() {
        val price = promo.calculatePrice(5, unitPrice)

        assertThat(price.toPence()).isEqualTo(100)
    }

    @Test
    fun `calculate price for zero quantity`() {
        val price = promo.calculatePrice(0, unitPrice)

        assertThat(price.toPence()).isZero()
    }
}