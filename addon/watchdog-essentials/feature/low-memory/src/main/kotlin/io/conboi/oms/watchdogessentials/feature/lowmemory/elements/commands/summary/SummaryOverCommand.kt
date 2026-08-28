package io.conboi.oms.watchdogessentials.feature.lowmemory.elements.commands.summary

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.lang.OmsLang
import io.conboi.oms.common.text.ComponentStyles.bold
import io.conboi.oms.common.text.ComponentStyles.color
import io.conboi.oms.common.text.ComponentStyles.literal
import io.conboi.oms.watchdogessentials.feature.lowmemory.LowMemoryFeature
import io.conboi.oms.watchdogessentials.feature.lowmemory.elements.commands.summary.SummaryCommand.Companion.formatBytesPercent
import io.conboi.oms.watchdogessentials.feature.lowmemory.elements.commands.summary.SummaryCommand.Companion.label
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.ByteFormatter
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.RetentionSummary
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

class SummaryOverCommand(private val feature: LowMemoryFeature) : OMSCommandEntry() {

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        val windowArgument = Commands.argument("window", StringArgumentType.word())
            .suggests { _, builder ->
                listOf("1m", "5m", "10m", "1h").forEach(builder::suggest)
                builder.buildFuture()
            }
            .executes { ctx ->
                val window = StringArgumentType.getString(ctx, "window")
                requestSummaryOver(ctx, window)
            }

        return Commands.literal("over")
            .executes { ctx -> requestSummaryOver(ctx, null) }
            .then(windowArgument)
    }

    fun requestSummaryOver(ctx: CommandContext<CommandSourceStack>, window: String?): Int {
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

        val defaultWindow = feature.averagingWindow
        var retentionWindow: Duration
        if (window == null) {
            source.sendSystemMessage(
                OmsLang.translatable(
                    "watchdogessentials.command.low_memory.summary_over.default_window",
                    TimeFormatter.formatDuration(defaultWindow)
                )
            )
            retentionWindow = defaultWindow
        } else {
            val parsedWindowDuration = TimeFormatter.parseToDurationOrNull(window)
                ?: run {
                    source.sendFailure(
                        OmsLang.translatable(
                            "watchdogessentials.command.low_memory.summary_over.invalid_window",
                            window
                        )
                    )
                    return 0
                }
            if (parsedWindowDuration <= 0.seconds) {
                source.sendFailure(
                    OmsLang.translatable(
                        "watchdogessentials.command.low_memory.summary_over.non_positive_window",
                        window
                    )
                )
                return 0
            }
            retentionWindow = parsedWindowDuration
        }

        if (retentionWindow > defaultWindow) {
            source.sendSystemMessage(
                OmsLang.translatable(
                    "watchdogessentials.command.low_memory.summary_over.window_exceeds_max",
                    TimeFormatter.formatDuration(retentionWindow),
                    TimeFormatter.formatDuration(defaultWindow)
                )
            )
            retentionWindow = defaultWindow
        }

        source.sendSuccess(
            { feature.requestSummaryOver(retentionWindow).toComponent() },
            false
        )

        return Command.SINGLE_SUCCESS
    }

    private fun RetentionSummary.toComponent(): Component {
        return Component.empty()
            .append("=== Low Memory History ===\n".literal().bold().color(ChatFormatting.GOLD))

            .append("\nWindow\n".literal().bold().color(ChatFormatting.YELLOW))
            .append(label("Duration"))
            .append("\n")
            .append(TimeFormatter.formatDuration(retentionWindow).literal().bold())
            .append("\n")
            .append(label("Snapshots"))
            .append("\n")
            .append(snapshotsCount.toString().literal().bold())
            .append("\n")

            .append("\nAverage\n".literal().bold().color(ChatFormatting.YELLOW))
            .append(label("Used"))
            .append("\n")
            .append(formatBytesPercent(averageUsedBytes, averageUsedPercent))
            .append("\n")
            .append(label("Available"))
            .append("\n")
            .append(formatBytesPercent(averageAvailableBytes, averageAvailablePercent))
            .append("\n")

            .append("\nExtremes\n".literal().bold().color(ChatFormatting.YELLOW))
            .append(label("Highest Used"))
            .append("\n")
            .append(ByteFormatter.format(maxUsedBytes).literal().bold())
            .append("\n")
            .append(label("Lowest Available"))
            .append("\n")
            .append(ByteFormatter.format(minAvailableBytes).literal().bold())
    }
}