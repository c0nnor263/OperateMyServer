package io.conboi.operatemyserver.feature.autorestart.elements.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.conboi.operatemyserver.common.content.StopManager
import io.conboi.operatemyserver.common.foundation.StopState
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands


class RestartCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("restart")
            .executes { ctx ->
                val server = ctx.source.server
                StopManager.stop(server, StopState.MANUAL)
                Command.SINGLE_SUCCESS
            }
    }
}