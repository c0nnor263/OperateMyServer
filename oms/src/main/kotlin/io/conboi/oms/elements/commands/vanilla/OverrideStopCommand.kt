package io.conboi.oms.elements.commands.vanilla

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import io.conboi.oms.common.foundation.reason.RegularStop
import io.conboi.oms.content.StopManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

internal class OverrideStopCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.root.children.removeIf { it.name == "stop" }

        dispatcher.register(
            Commands.literal("stop")
                .requires { source -> source.hasPermission(4) }
                .executes { context ->
                    val source = context.source
                    StopManager.writeReason(RegularStop)
                    source.sendSuccess({ Component.translatable("commands.stop.stopping") }, true)
                    source.server.halt(false)
                    Command.SINGLE_SUCCESS
                }
        )
    }
}