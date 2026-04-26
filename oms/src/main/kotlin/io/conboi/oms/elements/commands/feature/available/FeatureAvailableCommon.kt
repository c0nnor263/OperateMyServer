package io.conboi.oms.elements.commands.feature.available

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.OmsAddons
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.common.text.ComponentStyles.color
import io.conboi.oms.common.text.ComponentStyles.literal
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

class FeatureAvailableCommon {
    fun execute(ctx: CommandContext<CommandSourceStack>, available: Boolean): Int {
        val source = ctx.source
        val name = ctx.nodes[ctx.nodes.size - 2].node.name
        val (modId, featureId) = name.split(":")

        val addonInstance = OmsAddons.get(modId)
        if (addonInstance == null) {
            source.sendFailure(Component.translatable("oms.command.feature.not_found", name))
            return 0
        }

        val feature = addonInstance.context.featureManager.getFeatureById<OmsFeature<*>>(featureId)
        if (feature == null) {
            source.sendFailure(Component.translatable("oms.command.feature.not_found", name))
            return 0
        }

        if (available) {
            enable(ctx, feature, name)
        } else {
            disable(ctx, feature, name)
        }
        return Command.SINGLE_SUCCESS
    }

    fun enable(ctx: CommandContext<CommandSourceStack>, feature: OmsFeature<*>, name: String) {
        val source = ctx.source

        if (feature.isEnabled()) {
            source.sendFailure(
                Component.translatable("oms.command.feature.already_enabled", name),
            )
            return
        }

        feature.enable()
        source.sendSuccess(
            {
                Component.translatable(
                    "oms.command.feature.enabled", name
                        .literal()
                        .color(
                            ChatFormatting.GREEN
                        )
                )
            },
            true
        )
    }

    fun disable(ctx: CommandContext<CommandSourceStack>, feature: OmsFeature<*>, name: String) {
        val source = ctx.source
        if (!feature.isEnabled()) {
            source.sendFailure(
                Component.translatable("oms.command.feature.already_disabled", name)
            )
            return
        }

        feature.disable()
        source.sendSuccess(
            {
                Component.translatable(
                    "oms.command.feature.disabled",
                    name
                        .literal()
                        .color(
                            ChatFormatting.GREEN
                        )
                )
            },
            true
        )
    }
}