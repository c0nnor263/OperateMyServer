package io.conboi.oms.event

import net.minecraft.server.MinecraftServer
import net.minecraftforge.eventbus.api.Event
import org.jetbrains.annotations.ApiStatus

// internal only
@ApiStatus.Internal
abstract class OMSLifecycleInternal() : Event() {
    object Feature {
        class RegisterEvent : OMSLifecycleInternal()
    }

    object Server {
        data class ReadyEvent(val server: MinecraftServer) : OMSLifecycleInternal()
        data class PreShutdownEvent(val server: MinecraftServer) : OMSLifecycleInternal()
    }
}