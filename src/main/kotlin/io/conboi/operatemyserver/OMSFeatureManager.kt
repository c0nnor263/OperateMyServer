package io.conboi.operatemyserver

import io.conboi.operatemyserver.common.OperateMyServer
import io.conboi.operatemyserver.common.content.StopManager
import io.conboi.operatemyserver.common.foundation.feature.OmsFeature
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent

object OMSFeatureManager {
    private val active = mutableListOf<OmsFeature<*>>()

    fun activateAll(features: List<OmsFeature<*>>, event: ServerStartedEvent) {
        active.clear()
        active.addAll(features)
        active.forEach {
            try {
                it.onServerStarted(event)
            } catch (t: Throwable) {
                OperateMyServer.LOGGER.error("Feature start failed: ${it.javaClass.simpleName}", t)
            }
        }
    }

    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (StopManager.isServerStopping()) return
        active.forEach { feature ->
            if (feature.isEnabled()) {
                feature.onServerTick(event)
            }
        }
    }

    fun onServerStopping(event: ServerStoppingEvent) {
        active.forEach { it.onServerStopping(event) }
        active.clear()
    }
}