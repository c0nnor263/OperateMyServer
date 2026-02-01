package io.conboi.oms.watchdogessentials.addon.lowtps

import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.watchdogessentials.addon.lowtps.foundation.TpsMonitor
import io.conboi.oms.watchdogessentials.addon.lowtps.foundation.reason.LowTpsStop
import io.conboi.oms.watchdogessentials.addon.lowtps.infrastructure.config.CLowTpsFeature
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class LowTpsFeature(
    configProvider: ConfigProvider<CLowTpsFeature>
) : OmsFeature<CLowTpsFeature>(configProvider) {

    override fun info(): FeatureInfo {
        return super.info().copy(
            id = CLowTpsFeature.NAME,
            priority = Priority.CRITICAL,
        )
    }

    val tpsCountTime = configField {
        key = { config.tpsCountTime.get() }
        value = {
            TimeFormatter.parseToDurationOrNull(config.tpsCountTime.get())
                ?: error("Cannot parse tpsCountTime")
        }
    }

    val tpsThreshold = configField {
        key = { config.tpsThreshold.get() }
        value = { config.tpsThreshold.get() }
    }

    override fun onOmsTick(event: OMSLifecycle.TickingEvent, context: AddonContext) {
        super.onOmsTick(event, context)
        val server = event.server
        TpsMonitor.update(server)
        val avgTps = TpsMonitor.averageTpsOver(tpsCountTime.get())
        if (avgTps < tpsThreshold.get()) {
            LOG.warn("Low TPS detected (avg=$avgTps) for ${TimeFormatter.formatDuration(tpsCountTime.get())}s, threshold is ${config.tpsThreshold.get()}")
            FORGE_BUS.post(OMSActions.StopRequestedEvent(server, LowTpsStop))
        }
    }
}