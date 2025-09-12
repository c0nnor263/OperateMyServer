package io.conboi.oms.common

import io.conboi.oms.common.content.StopManager
import io.conboi.oms.common.foundation.TickTimer
import io.conboi.oms.common.foundation.feature.FeatureInfo
import io.conboi.oms.common.foundation.feature.OmsFeature
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent

object OMSFeatureManager {
    private val registry = mutableMapOf<FeatureInfo.Type, OmsFeature<*>>()
    val features get() = registry.values.toList()

    private val tickTimer = TickTimer()

    fun register(feature: OmsFeature<*>) {
        val type = feature.featureInfo.type
        require(registry.putIfAbsent(type, feature) == null) {
            "Feature '$type' already registered"
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : OmsFeature<*>?> getFeatureByType(type: FeatureInfo.Type): T? = registry[type] as T?

    fun getFeatureByName(name: String): OmsFeature<*>? {
        val type = runCatching {
            FeatureInfo.Type.valueOf(name)
        }.getOrNull() ?: return null
        return getFeatureByType(type)
    }

    fun activateAll(event: ServerStartedEvent) {
        features
            .sortedByDescending { it.featureInfo.priority }
            .forEach {
                try {
                    it.onServerStarted(event)
                } catch (t: Throwable) {
                    OperateMyServer.LOGGER.error("Feature start failed: ${it.javaClass.simpleName}", t)
                }
            }
    }

    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (!tickTimer.shouldFire(event.server.tickCount)) return
        features.forEach { feature ->
            if (StopManager.isServerStopping()) return
            if (feature.isEnabled()) {
                feature.onServerTick(event)
            }
        }
    }

    fun onServerStopping(event: ServerStoppingEvent) {
        features.forEach { it.onServerStopping(event) }
        registry.clear()
    }
}