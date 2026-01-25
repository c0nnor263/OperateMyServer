package io.conboi.oms.common.text

import io.conboi.oms.common.text.ComponentStyles.bold
import io.conboi.oms.common.text.ComponentStyles.color
import io.conboi.oms.common.text.ComponentStyles.literal
import io.kotest.core.spec.style.ShouldSpec
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.TextColor

class ComponentStylesTest : ShouldSpec({
    context("literal") {
        should("create a literal MutableComponent from string") {
            val str = "Hello, World!"
            val component = str.literal()
            assert(component.string == str)
        }
    }

    context("bold") {
        should("make a MutableComponent bold") {
            val value = "Bold Text"
            val component = value.literal().bold()
            assert(component.style.isBold)
        }
    }

    context("color") {
        should("set the color of a MutableComponent") {
            val value = "Colored Text"
            val color = ChatFormatting.RED
            val component = value.literal().color(ChatFormatting.RED)
            val expectedResult = TextColor.fromLegacyFormat(color)
            assert(component.style.color == expectedResult)
        }
    }
})
