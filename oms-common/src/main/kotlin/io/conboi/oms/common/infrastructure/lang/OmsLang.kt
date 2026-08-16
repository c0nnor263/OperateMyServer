package io.conboi.oms.common.infrastructure.lang

import java.util.Optional
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer

object OmsLang {
    fun translatable(
        key: String,
        vararg arguments: Any?
    ): MutableComponent {
        val translated = Component.translatable(key, *arguments)
        val result = Component.empty()

        translated.visit(
            { style, text ->
                result.append(
                    Component.literal(text).withStyle(style)
                )

                Optional.empty<Unit>()
            },
            Style.EMPTY
        )

        return result
    }

    fun translatable(
        player: ServerPlayer,
        key: String,
        vararg arguments: Any?
    ): Component {
        // TODO: Will be implemented in the future
        val locale = player.language
        return translatable(key, *arguments)
    }
}