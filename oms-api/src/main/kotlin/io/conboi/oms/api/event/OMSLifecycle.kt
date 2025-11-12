package io.conboi.oms.api.event

import io.conboi.oms.api.foundation.reason.StopReason
import net.minecraft.server.MinecraftServer
import net.minecraftforge.eventbus.api.Event

abstract class OMSLifecycle() : Event() {

    object Feature {
        class RegisterConfigEvent : OMSLifecycle()
        class RegisterEvent : OMSLifecycle()
    }

    data class StartingEvent(
        val server: MinecraftServer,
    ) : OMSLifecycle()

    data class TickingEvent(
        val server: MinecraftServer,
        val isServerStopping: Boolean
    ) : OMSLifecycle()

    data class StoppingEvent(
        val server: MinecraftServer,
    ) : OMSLifecycle()

    data class StopRequestedEvent(
        val server: MinecraftServer,
        val reason: StopReason,
    ) : OMSLifecycle()
}
