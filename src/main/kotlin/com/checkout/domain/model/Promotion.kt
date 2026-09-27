package com.checkout.domain.model

sealed interface Promotion {

    fun priceLine(quantity: Int, unitPrice: Money): Money

    data class MultiPrice(
        val quantity: Int,
        val price: Money
    ) : Promotion {
        init {
            require(quantity > 0) { "Multi-price quantity must be positive" }
        }

        override fun priceLine(quantity: Int, unitPrice: Money): Money {
            val groups = quantity / this.quantity
            val remainder = quantity % this.quantity
            return price.times(groups).plus(unitPrice.times(remainder))
        }
    }

    data class BuyNGetOneFree(
        val buyQuantity: Int
    ) : Promotion {
        init {
            require(buyQuantity >= 1) { "Buy quantity must be at least 1" }
        }

        override fun priceLine(quantity: Int, unitPrice: Money): Money {
            val freeItems = quantity / (buyQuantity + 1)
            return unitPrice.times(quantity - freeItems)
        }
    }

    data class MealDeal(
        val partnerSku: Sku,
        val dealPrice: Money
    ) : Promotion {
        override fun priceLine(quantity: Int, unitPrice: Money): Money = unitPrice.times(quantity)

        fun priceBundle(quantity: Int, partnerQuantity: Int): Money = dealPrice.times(minOf(quantity, partnerQuantity))
    }

    object None : Promotion {
        override fun priceLine(quantity: Int, unitPrice: Money): Money = unitPrice.times(quantity)
    }
}
