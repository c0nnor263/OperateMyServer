package io.conboi.oms.common.event

import net.minecraft.server.MinecraftServer
import net.minecraftforge.eventbus.api.Event
import org.jetbrains.annotations.ApiStatus

// internal only
@ApiStatus.Internal
abstract class OMSLifecycleInternal() : Event() {
    class RegisterFeaturesEvent() : OMSLifecycleInternal()
    class ServerReadyEvent(val server: MinecraftServer) : OMSLifecycleInternal()
    class ServerPreShutdownEvent(val server: MinecraftServer) : OMSLifecycleInternal()
}

