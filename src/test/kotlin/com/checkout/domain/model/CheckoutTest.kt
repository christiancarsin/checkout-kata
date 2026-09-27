package com.checkout.domain.model

import com.checkout.adapter.repository.InMemoryPricingRuleRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CheckoutTest {

    private val pricingRules = InMemoryPricingRuleRepository().findAll()

    @Test
    fun `scan single item returns unit price`() {
        val checkout = Checkout.create(pricingRules).scan('A').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(50)
    }

    @Test
    fun `scan multiple different items returns sum of unit prices`() {
        val checkout = Checkout.create(pricingRules)
            .scan('A').getOrThrow().scan('B').getOrThrow().scan('C').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(150)
    }

    @Test
    fun `scan multiple same items applies multi-price promotion`() {
        val checkout = Checkout.create(pricingRules)
            .scan('A').getOrThrow().scan('A').getOrThrow().scan('A').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(130)
    }

    @Test
    fun `scan items in any order applies multi-price promotion`() {
        val checkout = Checkout.create(pricingRules)
            .scan('B').getOrThrow().scan('A').getOrThrow().scan('B').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(175)
    }

    @Test
    fun `scan items applies buy n get one free promotion`() {
        val checkout = Checkout.create(pricingRules)
            .scan('C').getOrThrow().scan('C').getOrThrow()
            .scan('C').getOrThrow().scan('C').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(75)
    }

    @Test
    fun `scan items applies meal deal promotion`() {
        val checkout = Checkout.create(pricingRules).scan('D').getOrThrow().scan('E').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(300)
    }

    @Test
    fun `scan items applies meal deal regardless of order`() {
        val checkout = Checkout.create(pricingRules).scan('E').getOrThrow().scan('D').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(300)
    }

    @Test
    fun `complex scan with multiple promotions`() {
        val checkout = Checkout.create(pricingRules)
            .scan('A').getOrThrow().scan('A').getOrThrow().scan('A').getOrThrow()
            .scan('B').getOrThrow().scan('B').getOrThrow()
            .scan('C').getOrThrow().scan('C').getOrThrow()
            .scan('C').getOrThrow().scan('C').getOrThrow()
            .scan('D').getOrThrow().scan('E').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(630)
    }

    @Test
    fun `empty checkout returns zero`() {
        val checkout = Checkout.create(pricingRules)

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
        val checkout = Checkout.create(pricingRules).scan("A").getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(50)
    }

    @Test
    fun `scan using char sku`() {
        val checkout = Checkout.create(pricingRules).scan('A').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(50)
    }

    @Test
    fun `chain multiple scans`() {
        val checkout = Checkout.create(pricingRules)
            .scan('A').getOrThrow()
            .scan('B').getOrThrow()
            .scan('C').getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(150)
    }

    @Test
    fun `meal deal totals the same in any scan order`() {
        val dFirst = Checkout.create(pricingRules)
            .scan('D').getOrThrow().scan('D').getOrThrow().scan('E').getOrThrow()
        val eFirst = Checkout.create(pricingRules)
            .scan('E').getOrThrow().scan('D').getOrThrow().scan('D').getOrThrow()

        assertThat(dFirst.total().getOrThrow().toPence()).isEqualTo(450)
        assertThat(eFirst.total().getOrThrow().toPence()).isEqualTo(450)
    }

    @Test
    fun `get cart from checkout`() {
        val checkout = Checkout.create(pricingRules).scan('A').getOrThrow().scan('A').getOrThrow()

        val cart = checkout.cart

        assertThat(cart[Sku.of('A')]).isEqualTo(2)
    }

    @Test
    fun `scan multiple items in one call`() {
        val checkout = Checkout.create(pricingRules).scan(Sku.of('A'), 3).getOrThrow()

        assertThat(checkout.total().getOrThrow().toPence()).isEqualTo(130)
    }

    @Test
    fun `scan returns failure for invalid quantity`() {
        val checkout = Checkout.create(pricingRules)

        val result = checkout.scan(Sku('A'), 0)

        assertThat(result).isEqualTo(Result.Failure(DomainError.InvalidQuantity(0)))
    }

    @Test
    fun `scan returns failure for unknown sku`() {
        val checkout = Checkout.create(pricingRules)

        val result = checkout.scan('Z')

        assertThat(result).isEqualTo(Result.Failure(DomainError.UnknownSku(Sku.of('Z'))))
    }

    @Test
    fun `scan returns failure for lowercase sku character`() {
        val checkout = Checkout.create(pricingRules)

        val result = checkout.scan('a')

        assertThat(result).isEqualTo(
            Result.Failure(DomainError.InvalidSku("a", "SKU must be uppercase"))
        )
    }

    @Test
    fun `scan returns failure for non letter sku character`() {
        val checkout = Checkout.create(pricingRules)

        val result = checkout.scan('1')

        assertThat(result).isEqualTo(
            Result.Failure(DomainError.InvalidSku("1", "SKU must be a letter"))
        )
    }

    @Test
    fun `scan returns failure for multi character sku string`() {
        val checkout = Checkout.create(pricingRules)

        val result = checkout.scan("AB")

        assertThat(result).isEqualTo(
            Result.Failure(DomainError.InvalidSku("AB", "SKU must be a single character"))
        )
    }

    @Test
    fun `scanning does not mutate previously returned checkouts`() {
        val base = Checkout.create(pricingRules)

        val withA = base.scan('A').getOrThrow()

        assertThat(base.total().getOrThrow().toPence()).isZero()
        assertThat(withA.total().getOrThrow().toPence()).isEqualTo(50)
    }

    @Test
    fun `create rejects duplicate sku pricing rules`() {
        val duplicates = listOf(
            PricingRule(Sku.of('A'), Money.pence(50)),
            PricingRule(Sku.of('A'), Money.pence(60))
        )

        assertThatThrownBy { Checkout.create(duplicates) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Duplicate SKU")
    }
}
