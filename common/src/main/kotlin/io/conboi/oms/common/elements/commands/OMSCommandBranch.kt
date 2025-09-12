package io.conboi.oms.common.elements.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack

abstract class OMSCommandBranch {
    protected abstract fun getCommands(): List<OMSCommandEntry>
    open fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        groupBuilder: LiteralArgumentBuilder<CommandSourceStack>? = null
    ) {
        groupBuilder?.let { builder ->
            getCommands().forEach { command ->
                val commandBuilder = command.build()
                builder.then(commandBuilder)
            }
            dispatcher.register(builder)
        }
    }
}