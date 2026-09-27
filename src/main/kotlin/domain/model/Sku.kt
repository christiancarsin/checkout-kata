package com.checkout.domain.model

data class Sku(val value: Char) {

    init {
        require(value.isLetter()) { "SKU must be a letter" }
        require(value.isUpperCase()) { "SKU must be uppercase" }
    }

    companion object {
        fun of(char: Char): Sku = Sku(char)

        fun of(string: String): Sku {
            require(string.length == 1) { "SKU must be a single character" }
            return Sku(string.first())
        }
    }

    override fun toString(): String = value.toString()
}