package io.conboi.oms.elements.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.conboi.oms.api.elements.commands.OMSCommandBranch
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.elements.commands.feature.FeatureCommand
import io.conboi.oms.elements.commands.utilities.RestartCommand
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

internal class OperateMyServerCommandBranch : OMSCommandBranch() {
    override fun getCommands(): List<OMSCommandEntry> {
        return listOf(
            RestartCommand(),
            FeatureCommand()
        )
    }

    override fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        groupBuilder: LiteralArgumentBuilder<CommandSourceStack>?
    ) {
        val builder = Commands.literal("oms")
            .requires { it.hasPermission(4) }
        super.register(dispatcher, builder)
    }
}
