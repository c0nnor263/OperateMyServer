package io.conboi.oms.elements.commands.feature

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class FeatureNameArgumentCommand : OMSCommandEntry() {

    override fun additionalCommands(): List<OMSCommandEntry> {
        return listOf(
            FeatureEnableCommand(),
            FeatureDisableCommand(),
        )
    }

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        val featureArg = Commands.argument("featureName", StringArgumentType.word())
            .suggests { _, builder ->
                OMSFeatureManagers.oms.prioritizedFeatures?.forEach { feature ->
                    builder.suggest(feature.featureInfo.type.id)
                }
                builder.buildFuture()
            }

        OMSFeatureManagers.oms.prioritizedFeatures?.forEach { feature ->
            val literal = Commands.literal(feature.featureInfo.type.id)

            feature.getFeatureCommands().forEach { literal.then(it.build()) }

            println("Registering feature command: ${feature.featureInfo.type.id}")
            featureArg.then(literal)
        }

        return featureArg
    }
}