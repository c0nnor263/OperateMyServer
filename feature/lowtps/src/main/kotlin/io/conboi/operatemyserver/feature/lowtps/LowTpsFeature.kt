package io.conboi.operatemyserver.feature.lowtps

import io.conboi.operatemyserver.common.OperateMyServer
import io.conboi.operatemyserver.common.content.StopManager
import io.conboi.operatemyserver.common.foundation.StopState
import io.conboi.operatemyserver.common.foundation.TimeFormatter
import io.conboi.operatemyserver.common.foundation.feature.FeatureInfo
import io.conboi.operatemyserver.common.foundation.feature.OmsFeature
import io.conboi.operatemyserver.feature.lowtps.foundation.TpsMonitor
import io.conboi.operatemyserver.feature.lowtps.infrastructure.config.CLowTpsFeature
import net.minecraftforge.event.TickEvent

class LowTpsFeature(featureConfig: CLowTpsFeature) :
    OmsFeature<CLowTpsFeature>(featureConfig, FeatureInfo(type = FeatureInfo.Type.LOW_TPS, priority = 0)) {

    override fun onServerTick(event: TickEvent.ServerTickEvent) {
        val server = event.server
        TpsMonitor.update(server)
        // TODO: Optimize by parsing once and storing the result
        val tpsCountTime = TimeFormatter.parseToDurationOrNull(featureConfig.tpsCountTime.get()) ?: return
        val avgTps = TpsMonitor.averageTpsOver(tpsCountTime)
        if (avgTps < featureConfig.tpsThreshold.get()) {
            OperateMyServer.LOGGER.warn("Low TPS detected (avg=$avgTps) for ${featureConfig.tpsCountTime.get()}s, threshold is ${featureConfig.tpsThreshold.get()}")
            StopManager.stop(server, StopState.LOW_TPS)
        }
    }
}