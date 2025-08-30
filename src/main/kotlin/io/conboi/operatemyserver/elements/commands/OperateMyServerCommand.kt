package io.conboi.operatemyserver.elements.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import io.conboi.operatemyserver.common.content.StopManager
import io.conboi.operatemyserver.common.foundation.StopState
import io.conboi.operatemyserver.feature.autorestart.elements.commands.RestartCommand
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component


// TODO: Create a common interface for Commands
class OperateMyServerCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        overrideStopCommand(dispatcher)
        dispatcher.register(
            Commands.literal("oms")
                .requires { it.hasPermission(4) }
                .then(RestartCommand().build())
        )
    }

    private fun overrideStopCommand(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.root.children.removeIf { it.name == "stop" }

        dispatcher.register(
            Commands.literal("stop")
                .requires { source -> source.hasPermission(4) }
                .executes { context ->
                    val source = context.source
                    StopManager.writeReason(StopState.STOP)
                    source.sendSuccess({ Component.translatable("commands.stop.stopping") }, true)
                    source.server.halt(false)
                    Command.SINGLE_SUCCESS
                }
        )
    }
}
