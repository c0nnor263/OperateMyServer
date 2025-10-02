package io.conboi.oms.common.elements.commands.feature

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.foundation.feature.OmsFeature
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

internal class FeatureDisableCommand : OMSCommandEntry() {

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("disable")
            .executes { ctx -> disableFeature(ctx) }
    }

    private fun disableFeature(ctx: CommandContext<CommandSourceStack>): Int {
        val name = ctx.nodes[ctx.nodes.size - 2].node.name
        val source = ctx.source
        val feature = OMSFeatureManagers.oms.getFeatureById<OmsFeature<*>>(name)

        if (feature == null) {
            source.sendFailure(Component.translatable("oms.command.feature.not_found", name))
            return 0
        }

        if (!feature.isEnabled()) {
            source.sendSuccess(
                { Component.translatable("oms.command.feature.already_disabled", name) },
                false
            )
            return Command.SINGLE_SUCCESS
        }

        feature.disable()
        source.sendSuccess(
            { Component.translatable("oms.command.feature.disabled", name) },
            true
        )
        return Command.SINGLE_SUCCESS
    }
}