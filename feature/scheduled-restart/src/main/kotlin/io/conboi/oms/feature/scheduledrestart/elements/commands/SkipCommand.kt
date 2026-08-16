package io.conboi.oms.feature.scheduledrestart.elements.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.lang.OmsLang
import io.conboi.oms.common.text.ComponentStyles.bold
import io.conboi.oms.common.text.ComponentStyles.literal
import io.conboi.oms.feature.scheduledrestart.ScheduledRestartFeature
import io.conboi.oms.feature.scheduledrestart.foundation.SkipResult
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class SkipCommand(private val feature: ScheduledRestartFeature) : OMSCommandEntry() {

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("skip")
            .executes { ctx -> skip(ctx) }
    }

    fun skip(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val featureName = feature.info().id

        if (!feature.isEnabled()) {
            source.sendFailure(
                OmsLang.translatable(
                    "oms.command.feature.not_enabled",
                    OmsLang.translatable(featureName)
                )
            )
            return 0
        }

        when (val result = feature.skip()) {
            is SkipResult.Skipped -> {
                source.sendSuccess(
                    {
                        OmsLang.translatable(
                            "oms.command.scheduledrestart.skip.success",
                            TimeFormatter.formatDateTime(result.skippedRestartTime).literal().bold(),
                            TimeFormatter.formatDateTime(result.nextRestartTime).literal().bold()
                        )
                    }, true
                )
            }

            is SkipResult.AlreadySkipped -> {
                source.sendFailure(
                    OmsLang.translatable(
                        "oms.command.scheduledrestart.skip.already_skipped",
                        TimeFormatter.formatDateTime(result.nextRestartTime).literal().bold(),
                    )
                )
            }
        }

        return Command.SINGLE_SUCCESS
    }
}