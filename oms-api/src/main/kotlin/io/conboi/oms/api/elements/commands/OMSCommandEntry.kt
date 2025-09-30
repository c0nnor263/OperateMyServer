package io.conboi.oms.api.elements.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import net.minecraft.commands.CommandSourceStack

abstract class OMSCommandEntry {
    protected open fun additionalCommands(): List<OMSCommandEntry> = emptyList()

    protected abstract fun init(): ArgumentBuilder<CommandSourceStack, *>

    fun build(): ArgumentBuilder<CommandSourceStack, *> {
        val baseCommand = init()
        additionalCommands().forEach { additionalCommand ->
            val additionalCommandBuilder = additionalCommand.build()
            baseCommand.then(additionalCommandBuilder)
        }
        return baseCommand
    }
}