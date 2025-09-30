package io.conboi.oms.elements.commands.feature

import com.mojang.brigadier.builder.ArgumentBuilder
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class FeatureCommand : OMSCommandEntry() {
    override fun additionalCommands(): List<OMSCommandEntry> {
        return listOf(
            FeatureNameArgumentCommand()
        )
    }

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("feature")
    }
}