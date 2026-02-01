package io.conboi.oms.elements.commands.feature.available

import com.mojang.brigadier.builder.ArgumentBuilder
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

internal class FeatureEnableCommand : OMSCommandEntry() {
    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("enable")
            .executes { ctx -> FeatureAvailableCommon().execute(ctx = ctx, available = true) }
    }
}
