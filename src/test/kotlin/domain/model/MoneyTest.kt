package com.checkout.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MoneyTest {

    @Test
    fun `create money from pence`() {
        val money = Money.pence(50)

        assertThat(money.toPence()).isEqualTo(50)
        assertThat(money.toPounds()).isEqualTo(0.50)
    }

    @Test
    fun `create money from pounds`() {
        val money = Money.pounds(1.30)

        assertThat(money.toPence()).isEqualTo(130)
        assertThat(money.toPounds()).isEqualTo(1.30)
    }

    @Test
    fun `add two money values`() {
        val money1 = Money.pence(50)
        val money2 = Money.pence(75)

        val result = money1.plus(money2)

        assertThat(result.toPence()).isEqualTo(125)
    }

    @Test
    fun `subtract money values`() {
        val money1 = Money.pence(100)
        val money2 = Money.pence(30)

        val result = money1.minus(money2)

        assertThat(result.toPence()).isEqualTo(70)
    }

    @Test
    fun `multiply money by quantity`() {
        val money = Money.pence(25)

        val result = money.times(4)

        assertThat(result.toPence()).isEqualTo(100)
    }

    @Test
    fun `compare money values`() {
        val money1 = Money.pence(50)
        val money2 = Money.pence(75)
        val money3 = Money.pence(50)

        assertThat(money1.compareTo(money2)).isNegative()
        assertThat(money2.compareTo(money1)).isPositive()
        assertThat(money1.compareTo(money3)).isZero()
    }

    @Test
    fun `money equality`() {
        val money1 = Money.pence(50)
        val money2 = Money.pence(50)
        val money3 = Money.pence(75)

        assertThat(money1).isEqualTo(money2)
        assertThat(money1).isNotEqualTo(money3)
        assertThat(money1.hashCode()).isEqualTo(money2.hashCode())
    }

    @Test
    fun `zero money constant`() {
        assertThat(Money.ZERO.toPence()).isZero()
    }
}