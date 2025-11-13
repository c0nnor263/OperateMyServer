package io.conboi.oms.api.event

import io.conboi.oms.api.annotation.PublishOnForgeBus
import net.minecraft.server.MinecraftServer
import net.minecraftforge.eventbus.api.Event

abstract class OMSLifecycle() : Event() {

    @PublishOnForgeBus
    data class StartingEvent(
        val server: MinecraftServer,
    ) : OMSLifecycle()

    data class TickingEvent(
        val server: MinecraftServer,
        val isServerStopping: Boolean
    ) : OMSLifecycle()

    @PublishOnForgeBus
    data class StoppingEvent(
        val server: MinecraftServer,
    ) : OMSLifecycle()
}
