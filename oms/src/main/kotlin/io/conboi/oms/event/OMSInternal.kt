package io.conboi.oms.event

import net.minecraft.server.MinecraftServer
import net.minecraftforge.eventbus.api.Event
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
internal abstract class OMSInternal : Event() {
    object Addon {
        class PrepareEvent : OMSInternal()
    }

    object Server {
        data class ReadyEvent(val server: MinecraftServer) : OMSInternal()
        data class PreShutdownEvent(val server: MinecraftServer) : OMSInternal()
    }
}