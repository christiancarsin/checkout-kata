package com.checkout.application.service

import com.checkout.adapter.repository.InMemoryPricingRuleRepository
import com.checkout.domain.model.DomainError
import com.checkout.domain.model.Money
import com.checkout.domain.model.PricingRule
import com.checkout.domain.model.Promotion
import com.checkout.domain.model.Result
import com.checkout.domain.model.Sku
import com.checkout.domain.port.PricingRuleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CheckoutServiceTest {

    private val repository = InMemoryPricingRuleRepository()

    @Test
    fun `service uses rules from the repository when none are provided`() {
        val customRules = listOf(
            PricingRule(Sku.of('X'), Money.pence(100), Promotion.MultiPrice(2, Money.pence(150)))
        )
        val service = CheckoutService(object : PricingRuleRepository {
            override fun findAll(): List<PricingRule> = customRules
        })
        val session = service.startCheckout().scan('X').getOrThrow().scan('X').getOrThrow()

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
    fun `scanning does not mutate previously returned sessions`() {
        val service = CheckoutService(repository)
        val session = service.startCheckout()

        val scanned = session.scan('A').getOrThrow()

        assertThat(session.total().getOrThrow().toPence()).isZero()
        assertThat(scanned.total().getOrThrow().toPence()).isEqualTo(50)
    }

    @Test
    fun `service session can scan using Sku object`() {
        val service = CheckoutService(repository)
        val session = service.startCheckout().scan(Sku.of('A')).getOrThrow().scan(Sku.of('B')).getOrThrow()

        assertThat(session.total().getOrThrow().toPence()).isEqualTo(125)
    }

    @Test
    fun `service session returns cart`() {
        val service = CheckoutService(repository)
        val session = service.startCheckout().scan('A').getOrThrow().scan('A').getOrThrow()

        val cart = session.cart

        assertThat(cart[Sku.of('A')]).isEqualTo(2)
    }
}