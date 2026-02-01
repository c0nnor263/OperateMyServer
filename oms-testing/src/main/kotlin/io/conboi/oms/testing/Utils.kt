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
    val t = (captured as MutableComponent).contents as TranslatableContents
    t.key shouldBe expectedKey

    if (expectedArgs.isNotEmpty()) {
        t.args.size shouldBe expectedArgs.size
        expectedArgs.forEachIndexed { index, expected ->
            compareComponentArg(t.args[index], expected)
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
