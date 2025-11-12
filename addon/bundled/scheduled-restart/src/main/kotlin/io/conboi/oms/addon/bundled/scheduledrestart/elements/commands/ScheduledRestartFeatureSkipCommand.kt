package io.conboi.oms.addon.bundled.scheduledrestart.elements.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.addon.bundled.scheduledrestart.ScheduledRestartFeature
import io.conboi.oms.addon.bundled.scheduledrestart.content.SkipResult
import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.utils.foundation.TimeFormatter
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

class ScheduledRestartFeatureSkipCommand : OMSCommandEntry() {

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("skip")
            .executes { ctx -> skip(ctx) }
    }

    private fun skip(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val feature =
            OMSFeatureManagers.oms.getFeatureById<ScheduledRestartFeature>(CScheduledRestartFeature.Companion.NAME)
        val featureName = feature?.info?.id ?: CScheduledRestartFeature.NAME
        if (feature == null) {
            source.sendFailure(
                Component.translatable(
                    "oms.command.feature.not_found",
                    Component.translatable(featureName)
                )
            )
            return 0
        }

        if (!feature.isEnabled()) {
            source.sendFailure(
                Component.translatable(
                    "oms.command.feature.not_enabled",
                    Component.translatable(featureName)
                )
            )
            return 0
        }

        when (val result = feature.skip()) {
            is SkipResult.Skipped -> {
                source.sendSuccess(
                    {
                        Component.translatable(
                            "oms.command.autorestart.skip.success",
                            TimeFormatter.formatDateTime(result.skipped),
                            TimeFormatter.formatDateTime(result.next)
                        )
                    }, true
                )
            }

            is SkipResult.AlreadySkipped -> {
                source.sendFailure(
                    Component.translatable(
                        "oms.command.autorestart.skip.already_skipped",
                        TimeFormatter.formatDateTime(result.next),
                    )
                )
            }
        }

        return Command.SINGLE_SUCCESS
    }
}