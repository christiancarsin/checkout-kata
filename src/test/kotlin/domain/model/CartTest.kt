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
        val cart = Cart().addItem(Sku.of('A')).getOrThrow()

        assertThat(cart.getQuantity(Sku.of('A'))).isEqualTo(1)
        assertThat(cart.totalItems()).isEqualTo(1)
    }

    @Test
    fun `add multiple quantities of same item`() {
        val cart = Cart().addItem(Sku.of('A'), 3).getOrThrow()

        assertThat(cart.getQuantity(Sku.of('A'))).isEqualTo(3)
        assertThat(cart.totalItems()).isEqualTo(3)
    }

    @Test
    fun `cart calculates total with pricing rules`() {
        val cart = Cart().addItem(Sku.of('A'), 3).getOrThrow()

        val total = cart.calculateTotal(pricingRules)

        assertThat(total.getOrThrow().toPence()).isEqualTo(130)
    }

    @Test
    fun `cart item count`() {
        val cart = Cart().addItem(Sku.of('A'), 2).getOrThrow().addItem(Sku.of('B'), 3).getOrThrow()

        assertThat(cart.totalItems()).isEqualTo(5)
    }

    @Test
    fun `cart returns items grouped by sku`() {
        val cart = Cart().addItem(Sku.of('A'), 2).getOrThrow().addItem(Sku.of('B'), 1).getOrThrow()

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
        val cart = Cart().addItem(Sku.of('A')).getOrThrow()

        assertThat(cart.isEmpty()).isFalse()
    }

    @Test
    fun `returns failure for negative quantity`() {
        val result = Cart().addItem(Sku.of('A'), -1)

        assertThat(result).isEqualTo(Result.Failure(DomainError.InvalidQuantity(-1)))
    }

    @Test
    fun `returns failure for zero quantity`() {
        val result = Cart().addItem(Sku.of('A'), 0)

        assertThat(result).isEqualTo(Result.Failure(DomainError.InvalidQuantity(0)))
    }

    @Test
    fun `calculateTotal returns failure for unknown SKU`() {
        val cart = Cart().addItem(Sku.of('Z')).getOrThrow()
        val emptyRules = emptyMap<Sku, PricingRule>()

        val result = cart.calculateTotal(emptyRules)

        assertThat(result).isEqualTo(Result.Failure(DomainError.UnknownSku(Sku.of('Z'))))
    }
}