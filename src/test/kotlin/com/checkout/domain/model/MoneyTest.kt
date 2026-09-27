package com.checkout.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
    fun `create money from pounds does not lose pence to floating point`() {
        assertThat(Money.pounds(1.15).toPence()).isEqualTo(115)
        assertThat(Money.pounds(0.07).toPence()).isEqualTo(7)
        assertThat(Money.pounds(19.99).toPence()).isEqualTo(1999)
    }

    @Test
    fun `reject negative money`() {
        assertThatThrownBy { Money.pence(-1) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("negative")

        assertThatThrownBy { Money.pounds(-1.0) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("negative")
    }

    @Test
    fun `subtracting below zero is rejected`() {
        assertThatThrownBy { Money.pence(30).minus(Money.pence(100)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("negative")
    }

    @Test
    fun `formatted as pounds`() {
        assertThat(Money.pence(50).toString()).isEqualTo("£0.50")
        assertThat(Money.pence(1234).toString()).isEqualTo("£12.34")
        assertThat(Money.ZERO.toString()).isEqualTo("£0.00")
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
