package io.conboi.oms.elements.commands.feature

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.conboi.oms.api.OmsAddons
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.manager.FeatureManagerInfo
import io.conboi.oms.elements.commands.feature.available.FeatureDisableCommand
import io.conboi.oms.elements.commands.feature.available.FeatureEnableCommand
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

internal class FeatureCommand : OMSCommandEntry() {

    private val baseCommands by lazy {
        listOf(
            FeatureEnableCommand(),
            FeatureDisableCommand(),
        )
    }

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        val root = Commands.literal("feature")

        OmsAddons.forEachAddon { addon ->
            registerFeatureCommands(root, addon.info().featureManagerInfo)
        }

        return root
    }

    private fun registerFeatureCommands(
        root: LiteralArgumentBuilder<CommandSourceStack>,
        managerInfo: FeatureManagerInfo
    ) {
        managerInfo.featuresInfo.forEach { featureInfo ->
            val featureLiteralName = "${managerInfo.modId}:${featureInfo.id}"
            root.then(buildFeatureLiteral(featureLiteralName, featureInfo))
        }
    }

    private fun buildFeatureLiteral(
        featureLiteralName: String,
        featureInfo: FeatureInfo
    ): LiteralArgumentBuilder<CommandSourceStack> {
        val literal = Commands.literal(featureLiteralName)

        featureInfo.commands.forEach {
            literal.then(it.build())
        }

        baseCommands.forEach {
            literal.then(it.build())
        }

        return literal
    }
}