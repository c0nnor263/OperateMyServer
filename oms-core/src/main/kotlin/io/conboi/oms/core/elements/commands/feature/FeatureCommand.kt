package io.conboi.oms.core.elements.commands.feature

import com.mojang.brigadier.builder.ArgumentBuilder
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

internal class FeatureCommand : OMSCommandEntry() {
    private val baseCommands = listOf(
        FeatureEnableCommand(),
        FeatureDisableCommand(),
    )

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        val featureArg = Commands.literal("feature")
        OMSFeatureManagers.oms.prioritizedFeatures?.forEach { feature ->
            val featureLiteral = Commands.literal(feature.info.id)

            feature.getFeatureCommands().forEach { featureLiteral.then(it.build()) }

            baseCommands.forEach {
                featureLiteral.then(it.build())
            }

            featureArg.then(featureLiteral)
        }

        return featureArg
    }
}