package io.conboi.oms.event

import net.minecraft.server.MinecraftServer
import net.minecraftforge.eventbus.api.Event
import org.jetbrains.annotations.ApiStatus

// internal only
@ApiStatus.Internal
abstract class OMSInternal() : Event() {
    object Feature {
        class PrepareEvent : OMSInternal()
    }

    object Server {
        data class ReadyEvent(val server: MinecraftServer) : OMSInternal()
        data class PreShutdownEvent(val server: MinecraftServer) : OMSInternal()
    }
}