package io.conboi.oms.elements.commands.vanilla

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import io.conboi.oms.api.permission.PermissionLevel
import io.conboi.oms.api.permission.hasPermission
import io.conboi.oms.common.foundation.reason.RegularStop
import io.conboi.oms.common.infrastructure.lang.OmsLang
import io.conboi.oms.foundation.StopManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

internal class OverrideStopCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.root.children.removeIf { it.name == "stop" }

        dispatcher.register(
            Commands.literal("stop")
                .requires { source -> source.hasPermission(PermissionLevel.OWNER) }
                .executes { context ->
                    val source = context.source
                    StopManager.writeReason(RegularStop)
                    source.sendSuccess({ OmsLang.translatable("commands.stop.stopping") }, true)
                    source.server.halt(false)
                    Command.SINGLE_SUCCESS
                }
        )
    }
}