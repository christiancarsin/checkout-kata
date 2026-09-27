package com.checkout.application.service

import com.checkout.adapter.repository.InMemoryPricingRuleRepository
import com.checkout.domain.model.DomainError
import com.checkout.domain.model.Money
import com.checkout.domain.model.PricingRule
import com.checkout.domain.model.Result
import com.checkout.domain.model.Sku
import com.checkout.domain.port.PricingRuleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CheckoutServiceTest {

    private val repository = InMemoryPricingRuleRepository()

    @Test
    fun `scan items through service`() {
        val service = CheckoutService(repository)
        val session = service.startCheckout()

        session.scan('A').getOrThrow().scan('B').getOrThrow().scan('C').getOrThrow()

        assertThat(session.total().getOrThrow().toPence()).isEqualTo(150)
    }

    @Test
    fun `get total through service`() {
        val service = CheckoutService(repository)
        val session = service.startCheckout().scan('A').getOrThrow().scan('A').getOrThrow().scan('A').getOrThrow()

        val total = session.total()

        assertThat(total.getOrThrow().toPence()).isEqualTo(130)
    }

    @Test
    fun `service uses provided pricing rules`() {
        val customRules = listOf(
            PricingRule(Sku.of('X'), Money.pence(100), com.checkout.domain.model.Promotion.MultiPrice(2, Money.pence(150)))
        )
        val service = CheckoutService(object : PricingRuleRepository {
            override fun findAll(): List<PricingRule> = customRules
        })
        val session = service.startCheckout(customRules).scan('X').getOrThrow().scan('X').getOrThrow()

        assertThat(session.total().getOrThrow().toPence()).isEqualTo(150)
    }

    @Test
    fun `service creates new checkout per transaction`() {
        val service = CheckoutService(repository)
        val session1 = service.startCheckout().scan('A').getOrThrow()
        val session2 = service.startCheckout().scan('B').getOrThrow()

        assertThat(session1.total().getOrThrow().toPence()).isEqualTo(50)
        assertThat(session2.total().getOrThrow().toPence()).isEqualTo(75)
    }

    @Test
    fun `service session can scan using char`() {
        val service = CheckoutService(repository)
        val session = service.startCheckout()

        session.scan('A').getOrThrow().scan('B').getOrThrow()

        assertThat(session.total().getOrThrow().toPence()).isEqualTo(125)
    }

    @Test
    fun `service session can scan using string`() {
        val service = CheckoutService(repository)
        val session = service.startCheckout()

        session.scan("A").getOrThrow().scan("B").getOrThrow()

        assertThat(session.total().getOrThrow().toPence()).isEqualTo(125)
    }

    @Test
    fun `service session can scan using Sku object`() {
        val service = CheckoutService(repository)
        val session = service.startCheckout()

        session.scan(Sku.of('A')).getOrThrow().scan(Sku.of('B')).getOrThrow()

        assertThat(session.total().getOrThrow().toPence()).isEqualTo(125)
    }

    @Test
    fun `service session returns cart`() {
        val service = CheckoutService(repository)
        val session = service.startCheckout().scan('A').getOrThrow().scan('A').getOrThrow()

        val cart = session.getCart()

        assertThat(cart.getQuantity(Sku.of('A'))).isEqualTo(2)
    }

    @Test
    fun `service returns failure for unknown SKU`() {
        val service = CheckoutService(repository)
        val session = service.startCheckout()

        val result = session.scan('Z')

        assertThat(result).isEqualTo(Result.Failure(DomainError.UnknownSku(Sku.of('Z'))))
    }
}