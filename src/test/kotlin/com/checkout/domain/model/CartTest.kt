package com.checkout.domain.model

import com.checkout.adapter.repository.InMemoryPricingRuleRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CartTest {

    private val pricingRules = InMemoryPricingRuleRepository().findAll().associateBy { it.sku }

    @Test
    fun `empty cart has zero total`() {
        val cart = Cart()

        val total = cart.calculateTotal(pricingRules)

        assertThat(total.getOrThrow().toPence()).isZero()
    }

    @Test
    fun `add item to cart`() {
        val cart = Cart().addItem(Sku.of('A')).getOrThrow()

        assertThat(cart[Sku.of('A')]).isEqualTo(1)
        assertThat(cart.totalItems).isEqualTo(1)
    }

    @Test
    fun `add multiple quantities of same item`() {
        val cart = Cart().addItem(Sku.of('A'), 3).getOrThrow()

        assertThat(cart[Sku.of('A')]).isEqualTo(3)
        assertThat(cart.totalItems).isEqualTo(3)
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

        assertThat(cart.totalItems).isEqualTo(5)
    }

    @Test
    fun `cart returns items grouped by sku`() {
        val cart = Cart().addItem(Sku.of('A'), 2).getOrThrow().addItem(Sku.of('B'), 1).getOrThrow()

        val items = cart.items

        assertThat(items).hasSize(2)
        assertThat(items[Sku.of('A')]).isEqualTo(2)
        assertThat(items[Sku.of('B')]).isEqualTo(1)
    }

    @Test
    fun `cart items cannot be mutated through the exposed map`() {
        val cart = Cart().addItem(Sku.of('A'), 2).getOrThrow()

        (cart.items as MutableMap<Sku, Int>)[Sku.of('A')] = 99

        assertThat(cart[Sku.of('A')]).isEqualTo(2)
    }

    @Test
    fun `cart is empty initially`() {
        val cart = Cart()

        assertThat(cart.isEmpty).isTrue()
    }

    @Test
    fun `cart not empty after adding item`() {
        val cart = Cart().addItem(Sku.of('A')).getOrThrow()

        assertThat(cart.isEmpty).isFalse()
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
    fun `constructor rejects non-positive quantities`() {
        assertThatThrownBy { Cart(mapOf(Sku.of('A') to 0)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("positive")
    }

    @Test
    fun `calculateTotal returns failure for unknown SKU`() {
        val cart = Cart().addItem(Sku.of('Z')).getOrThrow()
        val emptyRules = emptyMap<Sku, PricingRule>()

        val result = cart.calculateTotal(emptyRules)

        assertThat(result).isEqualTo(Result.Failure(DomainError.UnknownSku(Sku.of('Z'))))
    }

    @Test
    fun `meal deal with extra items totals the same in any scan order - extra D`() {
        val dFirst = Cart().addItem(Sku.of('D'), 2).getOrThrow().addItem(Sku.of('E')).getOrThrow()
        val eFirst = Cart().addItem(Sku.of('E')).getOrThrow().addItem(Sku.of('D'), 2).getOrThrow()

        assertThat(dFirst.calculateTotal(pricingRules).getOrThrow().toPence()).isEqualTo(450)
        assertThat(eFirst.calculateTotal(pricingRules).getOrThrow().toPence()).isEqualTo(450)
    }

    @Test
    fun `meal deal with extra items totals the same in any scan order - extra E`() {
        val dFirst = Cart().addItem(Sku.of('D')).getOrThrow().addItem(Sku.of('E'), 2).getOrThrow()
        val eFirst = Cart().addItem(Sku.of('E'), 2).getOrThrow().addItem(Sku.of('D')).getOrThrow()

        assertThat(dFirst.calculateTotal(pricingRules).getOrThrow().toPence()).isEqualTo(500)
        assertThat(eFirst.calculateTotal(pricingRules).getOrThrow().toPence()).isEqualTo(500)
    }

    @Test
    fun `meal deal with multiple pairs and leftovers totals the same in any scan order`() {
        val dFirst = Cart().addItem(Sku.of('D'), 3).getOrThrow().addItem(Sku.of('E'), 2).getOrThrow()
        val eFirst = Cart().addItem(Sku.of('E'), 2).getOrThrow().addItem(Sku.of('D'), 3).getOrThrow()

        assertThat(dFirst.calculateTotal(pricingRules).getOrThrow().toPence()).isEqualTo(750)
        assertThat(eFirst.calculateTotal(pricingRules).getOrThrow().toPence()).isEqualTo(750)
    }

    @Test
    fun `meal deal declared on only one rule does not double charge`() {
        val oneSidedRules = pricingRules - Sku.of('E') +
            (Sku.of('E') to PricingRule(Sku.of('E'), Money.pence(200), Promotion.None))
        val dFirst = Cart().addItem(Sku.of('D')).getOrThrow().addItem(Sku.of('E')).getOrThrow()
        val eFirst = Cart().addItem(Sku.of('E')).getOrThrow().addItem(Sku.of('D')).getOrThrow()

        assertThat(dFirst.calculateTotal(oneSidedRules).getOrThrow().toPence()).isEqualTo(300)
        assertThat(eFirst.calculateTotal(oneSidedRules).getOrThrow().toPence()).isEqualTo(300)
    }

    @Test
    fun `meal deal with extra items and one-sided rule prices leftovers at unit price`() {
        val oneSidedRules = pricingRules - Sku.of('E') +
            (Sku.of('E') to PricingRule(Sku.of('E'), Money.pence(200), Promotion.None))
        val extraD = Cart().addItem(Sku.of('D'), 2).getOrThrow().addItem(Sku.of('E')).getOrThrow()
        val extraE = Cart().addItem(Sku.of('D')).getOrThrow().addItem(Sku.of('E'), 2).getOrThrow()
        val completePairs = Cart().addItem(Sku.of('D'), 2).getOrThrow().addItem(Sku.of('E'), 2).getOrThrow()

        assertThat(extraD.calculateTotal(oneSidedRules).getOrThrow().toPence()).isEqualTo(450)
        assertThat(extraE.calculateTotal(oneSidedRules).getOrThrow().toPence()).isEqualTo(500)
        assertThat(completePairs.calculateTotal(oneSidedRules).getOrThrow().toPence()).isEqualTo(600)
    }

    @Test
    fun `single meal deal item is charged at its own unit price`() {
        val cart = Cart().addItem(Sku.of('D')).getOrThrow()
        val eCart = Cart().addItem(Sku.of('E')).getOrThrow()

        assertThat(cart.calculateTotal(pricingRules).getOrThrow().toPence()).isEqualTo(150)
        assertThat(eCart.calculateTotal(pricingRules).getOrThrow().toPence()).isEqualTo(200)
    }
}
