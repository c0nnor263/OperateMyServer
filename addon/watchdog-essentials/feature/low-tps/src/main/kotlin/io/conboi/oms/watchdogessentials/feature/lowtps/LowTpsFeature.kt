package io.conboi.oms.watchdogessentials.feature.lowtps

import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.CachedField
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.cachedField
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import io.conboi.oms.watchdogessentials.feature.lowtps.foundation.TpsCalculator
import io.conboi.oms.watchdogessentials.feature.lowtps.foundation.TpsSnapshot
import io.conboi.oms.watchdogessentials.feature.lowtps.foundation.TpsSnapshotHistory
import io.conboi.oms.watchdogessentials.feature.lowtps.foundation.reason.LowTpsStop
import io.conboi.oms.watchdogessentials.feature.lowtps.infrastructure.config.CLowTpsFeature
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class LowTpsFeature(
    configProvider: ConfigProvider<CLowTpsFeature>
) : OmsFeature<CLowTpsFeature>(configProvider) {

    companion object {
        // FeatureManager dispatches tick callbacks on a 1-second cadence by default.
        private val TICK_TIMER_INTERVAL = 1.seconds
    }

    override fun info(): FeatureInfo {
        return super.info().copy(
            id = CLowTpsFeature.NAME,
            priority = Priority.CRITICAL,
        )
    }

    val tpsAveragingWindow: CachedField<String, Duration> = configField {
        key = { config.tpsAveragingWindow.get() }
        value = {
            TimeFormatter.parseToDurationOrNull(config.tpsAveragingWindow.get())
                ?: error("Cannot parse tpsAveragingWindow")
        }
    }

    val tpsThreshold: CachedField<Int, Int> = configField {
        key = { config.tpsThreshold.get() }
        value = { config.tpsThreshold.get() }
    }

    val tpsSnapshotHistory: TpsSnapshotHistory by cachedField {
        key = { tpsAveragingWindow.get() }
        value = {
            TpsSnapshotHistory(retentionWindow = tpsAveragingWindow.get(), checkInterval = TICK_TIMER_INTERVAL)
        }
    }
    private var stopRequested = false

    override fun onOmsTick(event: OMSLifecycle.TickingEvent, context: AddonContext) {
        super.onOmsTick(event, context)
        val server = event.server

        // Each tick becomes one sample in the rolling TPS window.
        val snapshot: TpsSnapshot = createSnapshot(server)
        tpsSnapshotHistory.add(snapshot)

        val averageTps = tpsSnapshotHistory.averageTps()
        if (!stopRequested && averageTps < tpsThreshold.get()) {
            LOG.warn(
                "Low TPS detected (avg=$averageTps) for ${TimeFormatter.formatDuration(tpsAveragingWindow.get())}, " +
                    "threshold is ${config.tpsThreshold.get()}"
            )
            stopRequested = true
            FORGE_BUS.post(OMSActions.StopRequestedEvent(server, LowTpsStop))
        }
    }

    private fun createSnapshot(server: MinecraftServer): TpsSnapshot {
        val now = TimeHelper.currentEpochSeconds
        return TpsSnapshot(
            createdAt = now,
            value = TpsCalculator.calculateGlobalTps(server)
        )
    }
}