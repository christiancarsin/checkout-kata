package com.checkout.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class SkuTest {

    @Test
    fun `create sku from valid character`() {
        val sku = Sku.of('A')

        assertThat(sku.value).isEqualTo('A')
        assertThat(sku.toString()).isEqualTo("A")
    }

    @Test
    fun `create sku from valid string`() {
        val sku = Sku.of("B")

        assertThat(sku.value).isEqualTo('B')
    }

    @Test
    fun `sku equality`() {
        val sku1 = Sku.of('A')
        val sku2 = Sku.of('A')
        val sku3 = Sku.of('B')

        assertThat(sku1).isEqualTo(sku2)
        assertThat(sku1).isNotEqualTo(sku3)
        assertThat(sku1.hashCode()).isEqualTo(sku2.hashCode())
    }

    @Test
    fun `sku toString returns character`() {
        val sku = Sku.of('C')

        assertThat(sku.toString()).isEqualTo("C")
    }

    @Test
    fun `throw exception for lowercase character`() {
        assertThatThrownBy { Sku.of('a') }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("uppercase")
    }

    @Test
    fun `throw exception for non-letter character`() {
        assertThatThrownBy { Sku.of('1') }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("letter")
    }

    @Test
    fun `throw exception for multi-character string`() {
        assertThatThrownBy { Sku.of("AB") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("single character")
    }

    @Test
    fun `throw exception for empty string`() {
        assertThatThrownBy { Sku.of("") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
