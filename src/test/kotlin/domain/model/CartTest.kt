package com.checkout.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CartTest {

    private val pricingRules = mapOf(
        Sku.of('A') to PricingRule(Sku.of('A'), Money.pence(50), Promotion.MultiPrice(3, Money.pence(130))),
        Sku.of('B') to PricingRule(Sku.of('B'), Money.pence(75), Promotion.MultiPrice(2, Money.pence(125))),
        Sku.of('C') to PricingRule(Sku.of('C'), Money.pence(25), Promotion.BuyNGetOneFree(3)),
        Sku.of('D') to PricingRule(Sku.of('D'), Money.pence(150), Promotion.MealDeal(Sku.of('E'), Money.pence(300))),
        Sku.of('E') to PricingRule(Sku.of('E'), Money.pence(200), Promotion.MealDeal(Sku.of('D'), Money.pence(300)))
    )

    @Test
    fun `empty cart has zero total`() {
        val cart = Cart()

        val total = cart.calculateTotal(pricingRules)

        assertThat(total.getOrThrow().toPence()).isZero()
    }

    @Test
    fun `add item to cart`() {
        val cart = Cart().addItem(Sku.of('A'))

        assertThat(cart.getQuantity(Sku.of('A'))).isEqualTo(1)
        assertThat(cart.totalItems()).isEqualTo(1)
    }

    @Test
    fun `add multiple quantities of same item`() {
        val cart = Cart().addItem(Sku.of('A'), 3)

        assertThat(cart.getQuantity(Sku.of('A'))).isEqualTo(3)
        assertThat(cart.totalItems()).isEqualTo(3)
    }

    @Test
    fun `cart calculates total with pricing rules`() {
        val cart = Cart().addItem(Sku.of('A'), 3)

        val total = cart.calculateTotal(pricingRules)

        assertThat(total.getOrThrow().toPence()).isEqualTo(130)
    }

    @Test
    fun `cart item count`() {
        val cart = Cart().addItem(Sku.of('A'), 2).addItem(Sku.of('B'), 3)

        assertThat(cart.totalItems()).isEqualTo(5)
    }

    @Test
    fun `cart returns items grouped by sku`() {
        val cart = Cart().addItem(Sku.of('A'), 2).addItem(Sku.of('B'), 1)

        val items = cart.getItems()

        assertThat(items).hasSize(2)
        assertThat(items[Sku.of('A')]).isEqualTo(2)
        assertThat(items[Sku.of('B')]).isEqualTo(1)
    }

    @Test
    fun `cart is empty initially`() {
        val cart = Cart()

        assertThat(cart.isEmpty()).isTrue()
    }

    @Test
    fun `cart not empty after adding item`() {
        val cart = Cart().addItem(Sku.of('A'))

        assertThat(cart.isEmpty()).isFalse()
    }

    @Test
    fun `throw exception for negative quantity`() {
        assertThatThrownBy { Cart().addItem(Sku.of('A'), -1) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("positive")
    }

    @Test
    fun `throw exception for zero quantity`() {
        assertThatThrownBy { Cart().addItem(Sku.of('A'), 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("positive")
    }

    @Test
    fun `calculateTotal returns failure for unknown SKU`() {
        val cart = Cart().addItem(Sku.of('Z'))
        val emptyRules = emptyMap<Sku, PricingRule>()

        val result = cart.calculateTotal(emptyRules)

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        val failure = result as Result.Failure<*>
        assertThat(failure.error).isInstanceOf(DomainError.UnknownSku::class.java)
    }
}