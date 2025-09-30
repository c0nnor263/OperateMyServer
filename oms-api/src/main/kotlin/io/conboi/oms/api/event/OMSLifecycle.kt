package io.conboi.oms.api.event

import io.conboi.oms.api.foundation.feature.FeatureRegistry
import net.minecraft.server.MinecraftServer
import net.minecraftforge.eventbus.api.Event

// TODO: Think about whole Feature lifecycle
abstract class OMSLifecycle() : Event() {
    class RegisterFeaturesConfigEvent() : OMSLifecycle()

    class RegisterFeaturesEvent(
        val registry: FeatureRegistry
    ) : OMSLifecycle()

    class StartingEvent(
        val server: MinecraftServer,
    ) : OMSLifecycle()

    class TickingEvent(
        val server: MinecraftServer,
    ) : OMSLifecycle()

    class StoppingEvent(
        val server: MinecraftServer,
    ) : OMSLifecycle()
}