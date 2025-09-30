package io.conboi.oms.common

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.FeatureRegistry
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.common.content.StopManager
import io.conboi.oms.common.foundation.TickTimer

object OMSFeatureManager : FeatureRegistry {
    private val registry = mutableMapOf<String, OmsFeature<*>>()
    val features get() = registry.values.sortedByDescending { it.featureInfo.priority }
    private var frozen = false

    private val tickTimer = TickTimer()

    override fun register(feature: OmsFeature<*>) {
        check(!frozen) { "Cannot register features after server has started!" }
        val id = feature.featureInfo.type.id
        require(!registry.containsKey(id)) {
            "Feature '$id' already registered"
        }
        registry[id] = feature
    }

    fun freeze() {
        frozen = true
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : OmsFeature<*>?> getFeatureByType(type: FeatureInfo.Type): T? = registry[type.id] as T?

    fun getFeatureById(id: String): OmsFeature<*>? {
        return registry[id]
    }

    fun onStartingEvent(event: OMSLifecycle.StartingEvent) {
        features.forEach {
            try {
                it.onOmsStarted(event)
            } catch (t: Throwable) {
                OperateMyServer.LOGGER.error("Feature start failed: ${it.javaClass.simpleName}", t)
            }
        }
    }

    fun onServerTick(event: OMSLifecycle.TickingEvent) {
        if (!tickTimer.shouldFire(event.server.tickCount)) return
        features.forEach { feature ->
            if (StopManager.isServerStopping()) return
            if (feature.isEnabled()) {
                feature.onOmsTick(event)
            }
        }
    }

    fun onServerStopping(event: OMSLifecycle.StoppingEvent) {
        features.forEach { it.onOmsStopping(event) }
        registry.clear()
    }
}