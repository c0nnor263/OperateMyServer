package io.conboi.oms.watchdogessentials.addon.lowtps

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.utils.foundation.TimeFormatter
import io.conboi.oms.watchdogessentials.addon.lowtps.foundation.TpsMonitor
import io.conboi.oms.watchdogessentials.addon.lowtps.foundation.reason.LowTpsStop
import io.conboi.oms.watchdogessentials.addon.lowtps.infrastructure.config.CLowTpsFeature
import io.conboi.oms.watchdogessentials.core.infrastructure.LOG
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

internal class LowTpsFeature : OmsFeature<CLowTpsFeature>() {

    override val info: FeatureInfo = FeatureInfo(
        id = CLowTpsFeature.NAME,
        priority = FeatureInfo.Priority.CRITICAL
    )

    val tpsCountTime = configField {
        key = { config.tpsCountTime.get() }
        value = {
            TimeFormatter.parseToDurationOrNull(config.tpsCountTime.get())
                ?: error("Cannot parse tpsCountTime")
        }
    }

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