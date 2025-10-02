package io.conboi.oms.feature.autorestart.elements.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.feature.autorestart.AutoRestartFeature
import io.conboi.oms.feature.autorestart.content.SkipResult
import io.conboi.oms.feature.autorestart.infrastructure.config.CAutoRestartFeature
import java.time.ZonedDateTime
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

class AutoRestartFeatureSkipCommand : OMSCommandEntry() {

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("skip")
            .executes { ctx -> skip(ctx) }
    }

    private fun skip(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val feature = OMSFeatureManagers.oms.getFeatureById<AutoRestartFeature>(CAutoRestartFeature.NAME)
        val featureName = feature?.info?.id ?: CAutoRestartFeature.NAME
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
                            formatTime(result.skipped),
                            formatTime(result.next)
                        )
                    }, true
                )
            }

            is SkipResult.AlreadySkipped -> {
                source.sendFailure(
                    Component.translatable(
                        "oms.command.autorestart.skip.already_skipped",
                        formatTime(result.next),
                    )
                )
            }
        }

        return Command.SINGLE_SUCCESS
    }

    fun formatTime(time: ZonedDateTime): String {
        val now = TimeHelper.currentTime
        val formatter = if (time.toLocalDate().isEqual(now.toLocalDate())) {
            TimeFormatter.HHmmFormatter
        } else {
            TimeFormatter.ddMMHHmmFormatter
        }
        return TimeFormatter.formatZonedDateTime(time, formatter)
    }

}