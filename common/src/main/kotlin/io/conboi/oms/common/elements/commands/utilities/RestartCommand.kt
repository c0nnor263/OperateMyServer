package io.conboi.oms.common.elements.commands.utilities

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.common.foundation.reason.ManualRestartStop
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

internal class RestartCommand : OMSCommandEntry() {
    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("restart")
            .executes { ctx ->
                val server = ctx.source.server
                FORGE_BUS.post(OMSLifecycle.StopRequestedEvent(server, ManualRestartStop))
                Command.SINGLE_SUCCESS
            }
    }
}