package io.conboi.oms.common.infrastructure.config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.minecraftforge.common.ForgeConfigSpec

class CValueTest : ShouldSpec({

    fun <T> mockConfigValue(initial: T): ForgeConfigSpec.ConfigValue<T> {
        val cv = mockk<ForgeConfigSpec.ConfigValue<T>>(relaxed = true)
        var value = initial
        every { cv.get() } answers { value }
        every { cv.set(any()) } answers { value = firstArg<Any>() as T }
        return cv
    }

    should("throw if accessed before registration") {
        val configValue = mockConfigValue(42)
        val cValue = CValue(
            name = "test",
            provider = { configValue }
        )

        shouldThrow<AssertionError> {
            cValue.get()
        }

        shouldThrow<AssertionError> {
            cValue.set(null)
        }
    }

    should("store ConfigValue on register") {
        val configValue = mockConfigValue(42)

        val cValue = CValue(
            name = "test",
            provider = { configValue }
        )

        val builder = mockk<ForgeConfigSpec.Builder>(relaxed = true)

        cValue.register(builder)

        cValue.get() shouldBe 42
    }

    should("delegate get() to underlying ConfigValue") {
        val configValue = mockConfigValue("abc")

        val cValue = CValue(
            name = "test",
            provider = { configValue }
        )

        val builder = mockk<ForgeConfigSpec.Builder>(relaxed = true)
        cValue.register(builder)

        cValue.get() shouldBe "abc"
    }

    should("delegate set() to underlying ConfigValue") {
        val configValue = mockConfigValue(1)

        val cValue = CValue(
            name = "test",
            provider = { configValue }
        )

        val builder = mockk<ForgeConfigSpec.Builder>(relaxed = true)
        cValue.register(builder)

        cValue.set(99)
        cValue.get() shouldBe 99
    }
})

