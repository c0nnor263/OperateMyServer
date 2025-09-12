package io.conboi.oms.common.foundation.feature

import io.conboi.oms.common.infrastructure.config.FeatureConfigBase
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent

abstract class OmsFeature<out T : FeatureConfigBase>(val featureConfig: T, val featureInfo: FeatureInfo) {
    fun isEnabled(): Boolean = featureConfig.enabled.get()

    fun enable() {
        featureConfig.enabled.set(true)
    }

    fun disable() {
        featureConfig.enabled.set(false)
    }

    open fun onServerTick(event: TickEvent.ServerTickEvent) {}
    open fun onServerStarted(event: ServerStartedEvent) {}
    open fun onServerStopping(event: ServerStoppingEvent) {}
}