package io.conboi.restartmyserver.elements.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object RestartCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("restart")
                .requires { it.hasPermission(4) }
                .executes { ctx ->
                    val msg = Component.literal("[RMS] /restart called (stub). Auto-restart not wired yet.")
                    ctx.source.server.playerList.broadcastSystemMessage(msg, false)
                    Command.SINGLE_SUCCESS
                }
        )
    }
}
