package io.conboi.oms.feature.lowtps

import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.common.content.StopManager
import io.conboi.oms.common.foundation.CachedField
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.feature.FeatureInfo
import io.conboi.oms.common.foundation.feature.OmsFeature
import io.conboi.oms.feature.lowtps.foundation.TpsMonitor
import io.conboi.oms.feature.lowtps.foundation.reason.LowTpsStop
import io.conboi.oms.feature.lowtps.infrastructure.config.CLowTpsFeature
import net.minecraftforge.event.TickEvent
import kotlin.time.Duration

internal class LowTpsFeature(featureConfig: CLowTpsFeature, featureInfo: FeatureInfo) :
    OmsFeature<CLowTpsFeature>(featureConfig, featureInfo) {

    val tpsCountTime: CachedField<String, Duration> = CachedField(
        key = { featureConfig.tpsCountTime.get() },
        value = {
            TimeFormatter.parseToDurationOrNull(featureConfig.tpsCountTime.get()) ?: error("Cannot parse tpsCountTime")
        }
    )

    override fun onServerTick(event: TickEvent.ServerTickEvent) {
        val server = event.server
        TpsMonitor.update(server)
        val avgTps = TpsMonitor.averageTpsOver(tpsCountTime.get())
        if (avgTps < featureConfig.tpsThreshold.get()) {
            OperateMyServer.LOGGER.warn("Low TPS detected (avg=$avgTps) for ${TimeFormatter.formatDuration(tpsCountTime.get())}s, threshold is ${featureConfig.tpsThreshold.get()}")
            StopManager.stop(server, LowTpsStop)
        }
    }
}