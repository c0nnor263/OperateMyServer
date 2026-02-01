package io.conboi.oms.elements.commands.feature.available

import com.mojang.brigadier.builder.ArgumentBuilder
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

internal class FeatureDisableCommand : OMSCommandEntry() {
    override fun init(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("disable")
            .executes { ctx -> FeatureAvailableCommon().execute(ctx = ctx, available = false) }
    }
}
