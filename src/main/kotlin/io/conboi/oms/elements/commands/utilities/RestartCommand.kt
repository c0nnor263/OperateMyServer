package io.conboi.oms.elements.commands.utilities

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.common.content.StopManager
import io.conboi.oms.common.foundation.reason.ManualStop
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class RestartCommand: OMSCommandEntry() {
    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("restart")
            .executes { ctx ->
                val server = ctx.source.server
                StopManager.stop(server, ManualStop)
                Command.SINGLE_SUCCESS
            }
    }
}