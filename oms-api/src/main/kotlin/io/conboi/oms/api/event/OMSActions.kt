package io.conboi.oms.api.event

import io.conboi.oms.api.annotation.PublishOnForgeBus
import io.conboi.oms.api.foundation.reason.StopReason
import net.minecraft.server.MinecraftServer
import net.minecraftforge.eventbus.api.Event

abstract class OMSActions() : Event() {
    @PublishOnForgeBus
    data class StopRequestedEvent(
        val server: MinecraftServer,
        val reason: StopReason,
    ) : OMSLifecycle()
}