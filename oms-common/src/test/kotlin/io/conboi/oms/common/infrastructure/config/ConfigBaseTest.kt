package io.conboi.oms.common.infrastructure.config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import net.minecraftforge.common.ForgeConfigSpec

class ConfigBaseTest : ShouldSpec({

    class TestConfig : ConfigBase() {
        override val name = "test"

        val enabled = b(true, "enabled")
        val count = i(5, 0, 10, "count")
        val count2 = i(3, 0, "count2")
        val ratio = f(0.5f, 0f, 1f, "ratio")
        val ratio2 = f(1.0f, 0f, "ratio2")
        val text = s("abc", "text")
        val mode = e(ExampleEnum.A, "mode")
        val list = list(listOf(1, 2), "list")
    }

    should("collect all declared values") {
        val cfg = TestConfig()
        cfg.allValues.map { it.name } shouldBe listOf(
            "enabled", "count", "count2", "ratio", "ratio2", "text", "mode", "list"
        )
    }

    should("throw when get before register") {
        val cfg = TestConfig()
        shouldThrow<AssertionError> { cfg.enabled.get() }
    }

    should("register all values") {
        val cfg = TestConfig()
        val builder = mockk<ForgeConfigSpec.Builder>(relaxed = true)

        cfg.registerAll(builder)
        cfg.allValues.forEach {
            it.get() shouldNotBe null
        }
    }

    should("support group") {
        class GroupConfig : ConfigBase() {
            override val name = "root"
            val group = group(1, "grp")
        }

        val cfg = GroupConfig()
        cfg.allValues.any { it is ConfigGroup } shouldBe true
    }

    should("support nested config") {
        class Parent : ConfigBase() {
            override val name = "parent"
            val child = nested(
                1,
                {
                    object : ConfigBase() {
                        override val name = "child"
                        val value = i(3, "value")
                    }
                }
            )
        }

        val cfg = Parent()
        cfg.children.size shouldBe 1

        val builder = mockk<ForgeConfigSpec.Builder>(relaxed = true)
        cfg.registerAll(builder)

        cfg.children.first()!!.allValues.first().get() shouldNotBe null
    }

    should("normalize values") {
        val cfg = TestConfig()

        cfg.normalizeValue(ExampleEnum.B) shouldBe "B"
        cfg.normalizeValue(listOf(ExampleEnum.A, 2)) shouldBe listOf("A", 2)
        cfg.normalizeValue(mapOf("x" to ExampleEnum.B)) shouldBe mapOf("x" to "B")
    }
})

private enum class ExampleEnum {
    A, B
}

