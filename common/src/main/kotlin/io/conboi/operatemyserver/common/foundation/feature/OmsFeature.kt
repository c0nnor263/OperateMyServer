package io.conboi.operatemyserver.common.foundation.feature

import io.conboi.operatemyserver.common.infrastructure.config.FeatureConfigBase
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent

// TODO: Add Feature Priority for execution order in OmsFeatureManager.tickAll
// TODO: Create verify feature fields
abstract class OmsFeature<out T : FeatureConfigBase>(val featureConfig: T, val featureInfo: FeatureInfo) {
    fun isEnabled(): Boolean = featureConfig.enabled.get()

    abstract fun onServerTick(event: TickEvent.ServerTickEvent)

    open fun onServerStarted(event: ServerStartedEvent) {}
    open fun onServerStopping(event: ServerStoppingEvent) {}
}