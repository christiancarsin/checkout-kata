package com.checkout.domain.model

sealed interface Promotion {
    fun calculatePrice(quantity: Int, unitPrice: Money): Money

    data class MultiPrice(
        val quantity: Int,
        val price: Money
    ) : Promotion {
        override fun calculatePrice(qty: Int, unitPrice: Money): Money {
            val groups = qty / quantity
            val remainder = qty % quantity
            return price.times(groups).plus(unitPrice.times(remainder))
        }
    }

    data class BuyNGetOneFree(
        val buyQuantity: Int
    ) : Promotion {
        override fun calculatePrice(qty: Int, unitPrice: Money): Money {
            val freeItems = qty / (buyQuantity + 1)
            val paidItems = qty - freeItems
            return unitPrice.times(paidItems)
        }
    }

    data class MealDeal(
        val partnerSku: Sku,
        val dealPrice: Money
    ) : Promotion {
        override fun calculatePrice(qty: Int, unitPrice: Money): Money {
            return unitPrice.times(qty)
        }
    }

    object None : Promotion {
        override fun calculatePrice(quantity: Int, unitPrice: Money): Money {
            return unitPrice.times(quantity)
        }
    }
}