package io.conboi.oms.elements.commands.feature

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import io.conboi.oms.common.elements.commands.OMSCommandEntry
import io.conboi.oms.common.foundation.feature.FeatureInfo
import io.conboi.oms.feature.autorestart.elements.commands.AutoRestartFeatureSkipCommand
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class FeatureNameArgumentCommand : OMSCommandEntry() {

    override fun additionalCommands(): List<OMSCommandEntry> {
        return listOf(
            FeatureEnableCommand(),
            FeatureDisableCommand(),
            AutoRestartFeatureSkipCommand()
        )
    }

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.argument("featureName", StringArgumentType.word())
            .suggests { _, builder ->
                FeatureInfo.Type.entries.forEach { builder.suggest(it.name) }
                builder.buildFuture()
            }
    }
}