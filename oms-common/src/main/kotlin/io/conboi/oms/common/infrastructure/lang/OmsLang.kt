package io.conboi.oms.common.infrastructure.lang

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object OmsLang {
    fun translatable(
        key: String,
        vararg arguments: Any?
    ): Component {
        return Component.literal(
            Component.translatable(key, *arguments).string
        )
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