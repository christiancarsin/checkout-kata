package com.checkout.domain.model

sealed interface DomainError {
    data class UnknownSku(val sku: Sku) : DomainError {
        override fun toString(): String = "Unknown SKU: $sku"
    }
    data class InvalidSku(val value: String, val reason: String) : DomainError {
        override fun toString(): String = "Invalid SKU: $value ($reason)"
    }
    data class InvalidQuantity(val quantity: Int) : DomainError {
        override fun toString(): String = "Invalid quantity: $quantity (must be positive)"
    }
}
