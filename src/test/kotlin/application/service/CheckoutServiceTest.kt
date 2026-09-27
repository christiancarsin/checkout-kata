package com.checkout.application.service

import com.checkout.domain.model.Money
import com.checkout.domain.model.PricingRule
import com.checkout.domain.model.Sku
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CheckoutServiceTest {

    @Test
    fun `scan items through service`() {
        val service = CheckoutService()
        val session = service.startCheckout()

        session.scan('A').scan('B').scan('C')

        assertThat(session.total().toPence()).isEqualTo(150)
    }

    @Test
    fun `get total through service`() {
        val service = CheckoutService()
        val session = service.startCheckout().scan('A').scan('A').scan('A')

        val total = session.total()

        assertThat(total.toPence()).isEqualTo(130)
    }

    @Test
    fun `service uses provided pricing rules`() {
        val customRules = listOf(
            PricingRule(Sku.of('X'), Money.pence(100), com.checkout.domain.model.Promotion.MultiPrice(2, Money.pence(150)))
        )
        val service = CheckoutService(customRules)
        val session = service.startCheckout().scan('X').scan('X')

        assertThat(session.total().toPence()).isEqualTo(150)
    }

    @Test
    fun `service creates new checkout per transaction`() {
        val service = CheckoutService()
        val session1 = service.startCheckout().scan('A')
        val session2 = service.startCheckout().scan('B')

        assertThat(session1.total().toPence()).isEqualTo(50)
        assertThat(session2.total().toPence()).isEqualTo(75)
    }

    @Test
    fun `service session can scan using char`() {
        val service = CheckoutService()
        val session = service.startCheckout()

        session.scan('A').scan('B')

        assertThat(session.total().toPence()).isEqualTo(125)
    }

    @Test
    fun `service session can scan using string`() {
        val service = CheckoutService()
        val session = service.startCheckout()

        session.scan("A").scan("B")

        assertThat(session.total().toPence()).isEqualTo(125)
    }

    @Test
    fun `service session can scan using Sku object`() {
        val service = CheckoutService()
        val session = service.startCheckout()

        session.scan(Sku.of('A')).scan(Sku.of('B'))

        assertThat(session.total().toPence()).isEqualTo(125)
    }

    @Test
    fun `service session returns cart`() {
        val service = CheckoutService()
        val session = service.startCheckout().scan('A').scan('A')

        val cart = session.getCart()

        assertThat(cart.getQuantity(Sku.of('A'))).isEqualTo(2)
    }
}