package com.checkout.domain.model

data class Money(private val pence: Long) : Comparable<Money> {

    companion object {
        val ZERO: Money = Money(0)

        fun pence(amount: Long): Money = Money(amount)

        fun pounds(amount: Double): Money = Money((amount * 100).toLong())
    }

    fun plus(other: Money): Money = Money(this.pence + other.pence)

    fun minus(other: Money): Money = Money(this.pence - other.pence)

    fun times(quantity: Int): Money = Money(this.pence * quantity)

    fun toPence(): Long = pence

    fun toPounds(): Double = pence / 100.0

    override fun compareTo(other: Money): Int = this.pence.compareTo(other.pence)

    override fun toString(): String = "£${String.format("%.2f", toPounds())}"
}