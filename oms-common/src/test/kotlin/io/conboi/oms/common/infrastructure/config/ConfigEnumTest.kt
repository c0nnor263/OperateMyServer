package io.conboi.oms.common.infrastructure.config

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import net.minecraftforge.common.ForgeConfigSpec

class ConfigEnumTest : ShouldSpec({
    fun register(value: CValue<*, *>) {
        val builder = mockk<ForgeConfigSpec.Builder>(relaxed = true)
        value.register(builder)
    }

    context("registration") {

        should("register enum config value without errors") {
            val enumValue = ConfigEnum(
                name = "test_enum",
                defaultValue = TestEnum.FIRST,
                comment = emptyArray()
            )

            register(enumValue)

            val field = enumValue
                .javaClass
                .superclass
                .getDeclaredField("value")
                .apply { isAccessible = true }
                .get(enumValue)

            field shouldNotBe null
        }
    }

    context("metadata") {

        should("expose correct name") {
            val enumValue = ConfigEnum(
                name = "my_enum",
                defaultValue = TestEnum.SECOND,
                comment = emptyArray()
            )

            enumValue.name shouldBe "my_enum"
        }
    }

    context("safety") {

        should("not allow get before registration") {
            val enumValue = ConfigEnum(
                name = "unsafe_enum",
                defaultValue = TestEnum.THIRD,
                comment = emptyArray()
            )

            val ex = kotlin.runCatching {
                enumValue.get()
            }.exceptionOrNull()

            ex shouldNotBe null
            (ex is AssertionError) shouldBe true
        }
    }
})

enum class TestEnum {
    FIRST,
    SECOND,
    THIRD
}
