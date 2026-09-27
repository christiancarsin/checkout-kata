package com.checkout.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MultiPricePromotionTest {

    private val unitPrice = Money.pence(50)
    private val promo = Promotion.MultiPrice(3, Money.pence(130))

    @Test
    fun `calculate price for exact multi-price quantity`() {
        val price = promo.priceLine(3, unitPrice)

        assertThat(price.toPence()).isEqualTo(130)
    }

    @Test
    fun `calculate price for quantity less than multi-price threshold`() {
        val price = promo.priceLine(2, unitPrice)

        assertThat(price.toPence()).isEqualTo(100)
    }

    @Test
    fun `calculate price for quantity greater than multi-price threshold`() {
        val price = promo.priceLine(4, unitPrice)

        assertThat(price.toPence()).isEqualTo(180)
    }

    @Test
    fun `calculate price for multiple multi-price groups`() {
        val price = promo.priceLine(6, unitPrice)

        assertThat(price.toPence()).isEqualTo(260)
    }

    @Test
    fun `calculate price for zero quantity`() {
        val price = promo.priceLine(0, unitPrice)

        assertThat(price.toPence()).isZero()
    }

    @Test
    fun `calculate price for single item`() {
        val price = promo.priceLine(1, unitPrice)

        assertThat(price.toPence()).isEqualTo(50)
    }
}
