package io.conboi.oms.feature.lowtps

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.common.foundation.CachedField
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.LOG
import io.conboi.oms.feature.lowtps.foundation.TpsMonitor
import io.conboi.oms.feature.lowtps.foundation.reason.LowTpsStop
import io.conboi.oms.feature.lowtps.infrastructure.config.CLowTpsFeature
import kotlin.time.Duration
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

internal class LowTpsFeature(featureInfo: FeatureInfo) :
    OmsFeature<CLowTpsFeature>(featureInfo) {

    val tpsCountTime: CachedField<String, Duration> = CachedField(
        key = { config.tpsCountTime.get() },
        value = {
            TimeFormatter.parseToDurationOrNull(config.tpsCountTime.get())
                ?: error("Cannot parse tpsCountTime")
        }
    )

    override fun onOmsTick(event: OMSLifecycle.TickingEvent) {
        val server = event.server
        TpsMonitor.update(server)
        val avgTps = TpsMonitor.averageTpsOver(tpsCountTime.get())
        if (avgTps < config.tpsThreshold.get()) {
            LOG.warn("Low TPS detected (avg=$avgTps) for ${TimeFormatter.formatDuration(tpsCountTime.get())}s, threshold is ${config.tpsThreshold.get()}")
            FORGE_BUS.post(OMSLifecycle.StopRequestedEvent(server, LowTpsStop))
        }
    }
}