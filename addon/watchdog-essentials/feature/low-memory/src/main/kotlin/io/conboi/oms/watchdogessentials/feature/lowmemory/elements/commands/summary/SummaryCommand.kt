package io.conboi.oms.watchdogessentials.feature.lowmemory.elements.commands.summary

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.common.infrastructure.lang.OmsLang
import io.conboi.oms.common.text.ComponentStyles.bold
import io.conboi.oms.common.text.ComponentStyles.color
import io.conboi.oms.common.text.ComponentStyles.literal
import io.conboi.oms.watchdogessentials.feature.lowmemory.LowMemoryFeature
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.ByteFormatter
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemorySnapshot
import java.util.Locale
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

class SummaryCommand(private val feature: LowMemoryFeature) : OMSCommandEntry() {

    companion object {
        fun label(value: String): Component {
            return "$value: ".padEnd(20).literal().color(ChatFormatting.GRAY)
        }

        fun formatBytesPercent(bytes: Long, percent: Double): Component {
            return "${ByteFormatter.format(bytes)} (${"%.1f".format(Locale.US, percent)}%)"
                .literal()
                .bold()
        }
    }

    override fun additionalCommands(): List<OMSCommandEntry> {
        return listOf(
            SummaryOverCommand(feature)
        )
    }

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("summary")
            .executes { ctx -> requestSummary(ctx) }
    }

    fun requestSummary(ctx: CommandContext<CommandSourceStack>): Int {
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

        source.sendSuccess(
            { feature.requestSummary().toComponent() },
            true
        )

        return Command.SINGLE_SUCCESS
    }

    private fun MemorySnapshot?.toComponent(): Component {
        return Component.empty()
            .append("=== Low Memory Summary ===\n".literal().bold().color(ChatFormatting.GOLD))
            .append("\nCurrent\n".literal().bold().color(ChatFormatting.YELLOW))
            .apply {
                val snapshot = this@toComponent
                if (snapshot == null) {
                    append("No memory snapshots available yet.\n".literal().color(ChatFormatting.YELLOW))
                    return@apply
                }

                append(label("Used"))
                append("\n")
                append(formatBytesPercent(snapshot.usedBytes, snapshot.usedPercent))
                append("\n")

                append(label("Available"))
                append("\n")
                append(formatBytesPercent(snapshot.availableBytes, snapshot.availablePercent))
                append("\n")

                append(label("Allocated"))
                append("\n")
                append(ByteFormatter.format(snapshot.allocatedBytes).literal().bold())
                append("\n")

                append(label("Maximum Heap"))
                append("\n")
                append(ByteFormatter.format(snapshot.maxBytes).literal().bold())
                append("\n")
            }
    }
}