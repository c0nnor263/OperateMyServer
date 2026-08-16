package io.conboi.oms.testing

import io.kotest.matchers.shouldBe
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.TranslatableContents


fun checkCapturedTranslationKey(
    captured: Component,
    expectedKey: String,
    vararg expectedArgs: Any?
) {
    val contents = (captured as MutableComponent).contents

    when (contents) {
        is TranslatableContents -> {
            contents.key shouldBe expectedKey

            if (expectedArgs.isNotEmpty()) {
                contents.args.size shouldBe expectedArgs.size

                expectedArgs.forEachIndexed { index, expected ->
                    compareComponentArg(contents.args[index], expected)
                }
            }
        }

        else -> {
            val expected = Component
                .translatable(expectedKey, *expectedArgs)
                .string

            captured.string shouldBe expected
        }
    }
}

private fun compareComponentArg(actual: Any?, expected: Any?) {
    when {
        expected is String -> {
            actual.toString() shouldBe expected
        }

        expected is Component && actual is Component -> {
            val a = (actual as MutableComponent).contents
            val e = (expected as MutableComponent).contents

            if (a is TranslatableContents && e is TranslatableContents) {
                a.key shouldBe e.key
                a.args.size shouldBe e.args.size
                a.args.zip(e.args).forEach { (aa, ee) ->
                    compareComponentArg(aa, ee)
                }
            } else {
                actual.toString() shouldBe expected.toString()
            }
        }

        expected == null -> {
            actual shouldBe null
        }

        else -> error("Unsupported argument type: ${expected.javaClass}")
    }
}
