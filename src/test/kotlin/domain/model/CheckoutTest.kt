package com.checkout.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CheckoutTest {

    @Test
    fun `scan single item returns unit price`() {
        val checkout = Checkout.create().scan('A').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(50)
    }

    @Test
    fun `scan multiple different items returns sum of unit prices`() {
        val checkout = Checkout.create().scan('A').getOrThrow().scan('B').getOrThrow().scan('C').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(150)
    }

    @Test
    fun `scan multiple same items applies multi-price promotion`() {
        val checkout = Checkout.create().scan('A').getOrThrow().scan('A').getOrThrow().scan('A').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(130)
    }

    @Test
    fun `scan items in any order applies multi-price promotion`() {
        val checkout = Checkout.create().scan('B').getOrThrow().scan('A').getOrThrow().scan('B').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(175)
    }

    @Test
    fun `scan items applies buy n get one free promotion`() {
        val checkout = Checkout.create().scan('C').getOrThrow().scan('C').getOrThrow().scan('C').getOrThrow().scan('C').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(75)
    }

    @Test
    fun `scan items applies meal deal promotion`() {
        val checkout = Checkout.create().scan('D').getOrThrow().scan('E').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(300)
    }

    @Test
    fun `scan items applies meal deal regardless of order`() {
        val checkout = Checkout.create().scan('E').getOrThrow().scan('D').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(300)
    }

    @Test
    fun `complex scan with multiple promotions`() {
        val checkout = Checkout.create()
            .scan('A').getOrThrow().scan('A').getOrThrow().scan('A').getOrThrow()  // 3 for 130
            .scan('B').getOrThrow().scan('B').getOrThrow()             // 2 for 125
            .scan('C').getOrThrow().scan('C').getOrThrow().scan('C').getOrThrow().scan('C').getOrThrow()  // buy 3 get 1 free = 75
            .scan('D').getOrThrow().scan('E').getOrThrow()             // meal deal = 300

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(630)
    }

    @Test
    fun `empty checkout returns zero`() {
        val checkout = Checkout.create()

        assertThat(checkout.total().getOrThrow().toPence()).isZero()
    }

    @Test
    fun `checkout with custom pricing rules`() {
        val customRules = listOf(
            PricingRule(Sku.of('X'), Money.pence(100), Promotion.MultiPrice(2, Money.pence(150)))
        )
        val checkout = Checkout.create(customRules).scan('X').getOrThrow().scan('X').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(150)
    }

    @Test
    fun `scan using string sku`() {
        val checkout = Checkout.create().scan("A").getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(50)
    }

    @Test
    fun `scan using char sku`() {
        val checkout = Checkout.create().scan('A').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(50)
    }

    @Test
    fun `chain multiple scans`() {
        val checkout = Checkout.create()
            .scan('A').getOrThrow()
            .scan('B').getOrThrow()
            .scan('C').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(150)
    }

    @Test
    fun `get cart from checkout`() {
        val checkout = Checkout.create().scan('A').getOrThrow().scan('A').getOrThrow()

        val cart = checkout.getCart()

        assertThat(cart.getQuantity(Sku.of('A'))).isEqualTo(2)
    }
}