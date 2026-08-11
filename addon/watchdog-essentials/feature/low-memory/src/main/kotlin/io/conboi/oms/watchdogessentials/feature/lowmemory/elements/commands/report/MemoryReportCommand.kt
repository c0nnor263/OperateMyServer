package io.conboi.oms.watchdogessentials.feature.lowmemory.elements.commands.report

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.watchdogessentials.feature.lowmemory.LowMemoryFeature
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

class MemoryReportCommand(private val feature: LowMemoryFeature) : OMSCommandEntry() {

    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("report")
            .executes { ctx -> requestReport(ctx) }
    }

    fun requestReport(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val featureName = feature.info().id

        if (!feature.isEnabled()) {
            source.sendFailure(
                Component.translatable(
                    "oms.command.feature.not_enabled",
                    Component.translatable(featureName)
                )
            )
            return 0
        }

        feature.requestReport()

        source.sendSuccess(
            {
                Component.translatable(
                    "watchdogessentials.command.low_memory.report.requested",
                )
            }, true
        )

        return Command.SINGLE_SUCCESS
    }
}