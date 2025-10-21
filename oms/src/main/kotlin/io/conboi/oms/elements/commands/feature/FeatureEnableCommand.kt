package io.conboi.oms.elements.commands.feature

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.foundation.feature.OmsFeature
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

internal class FeatureEnableCommand : OMSCommandEntry() {

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("enable")
            .executes { ctx -> enableFeature(ctx) }
    }

    private fun enableFeature(ctx: CommandContext<CommandSourceStack>): Int {
        val name = ctx.nodes[ctx.nodes.size - 2].node.name
        val source = ctx.source
        val feature = OMSFeatureManagers.oms.getFeatureById<OmsFeature<*>>(name)

        if (feature == null) {
            source.sendFailure(Component.translatable("oms.command.feature.not_found", name))
            return 0
        }

        if (feature.isEnabled()) {
            source.sendSuccess(
                { Component.translatable("oms.command.feature.already_enabled", name) },
                false
            )
            return Command.SINGLE_SUCCESS
        }

        feature.enable()
        source.sendSuccess(
            { Component.translatable("oms.command.feature.enabled", name) },
            true
        )
        return Command.SINGLE_SUCCESS
    }
}