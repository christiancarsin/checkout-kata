package com.checkout.domain.model

sealed interface DomainError {
    data class UnknownSku(val sku: Sku) : DomainError {
        override fun toString(): String = "Unknown SKU: $sku"
    }
    data class InvalidQuantity(val quantity: Int) : DomainError {
        override fun toString(): String = "Invalid quantity: $quantity (must be positive)"
    }
}