package com.checkout.domain.model

import java.util.Locale
import kotlin.math.roundToLong

data class Money(private val pence: Long) : Comparable<Money> {

    init {
        require(pence >= 0) { "Money must not be negative: $pence pence" }
    }

    companion object {
        private const val PENCE_PER_POUND = 100L

        val ZERO: Money = Money(0)

        fun pence(amount: Long): Money = Money(amount)

        fun pounds(amount: Double): Money {
            require(amount >= 0.0) { "Money must not be negative: $amount" }
            return Money((amount * PENCE_PER_POUND).roundToLong())
        }
    }

    fun plus(other: Money): Money = Money(this.pence + other.pence)

    fun minus(other: Money): Money = Money(this.pence - other.pence)

    fun times(quantity: Int): Money = Money(this.pence * quantity)

    fun toPence(): Long = pence

    fun toPounds(): Double = pence / PENCE_PER_POUND.toDouble()

    override fun compareTo(other: Money): Int = this.pence.compareTo(other.pence)

    override fun toString(): String = "£${String.format(Locale.UK, "%.2f", toPounds())}"
}
