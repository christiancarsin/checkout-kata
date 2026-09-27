package com.checkout.domain.model

sealed interface Promotion {
    fun calculatePrice(quantity: Int, unitPrice: Money, partnerQuantity: Int = 0): Money

    data class MultiPrice(
        val quantity: Int,
        val price: Money
    ) : Promotion {
        override fun calculatePrice(qty: Int, unitPrice: Money, partnerQuantity: Int): Money {
            val groups = qty / quantity
            val remainder = qty % quantity
            return price.times(groups).plus(unitPrice.times(remainder))
        }
    }

    data class BuyNGetOneFree(
        val buyQuantity: Int
    ) : Promotion {
        override fun calculatePrice(qty: Int, unitPrice: Money, partnerQuantity: Int): Money {
            val freeItems = qty / (buyQuantity + 1)
            val paidItems = qty - freeItems
            return unitPrice.times(paidItems)
        }
    }

    data class MealDeal(
        val partnerSku: Sku,
        val dealPrice: Money
    ) : Promotion {
        override fun calculatePrice(qty: Int, unitPrice: Money, partnerQuantity: Int): Money {
            val pairs = minOf(qty, partnerQuantity)
            val remainder = qty - pairs
            return dealPrice.times(pairs).plus(unitPrice.times(remainder))
        }
    }

    object None : Promotion {
        override fun calculatePrice(quantity: Int, unitPrice: Money, partnerQuantity: Int): Money {
            return unitPrice.times(quantity)
        }
    }
}