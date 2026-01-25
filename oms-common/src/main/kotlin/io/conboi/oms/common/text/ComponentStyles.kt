package io.conboi.oms.common.text

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

object ComponentStyles {

    fun String.literal(): MutableComponent {
        return Component.literal(this)
    }

    fun MutableComponent.bold(): MutableComponent {
        return withStyle { style: Style ->
            style.withBold(true)
        }
    }

    fun MutableComponent.color(color: ChatFormatting): MutableComponent {
        return withStyle { style: Style ->
            style.withColor(color)
        }
    }
}