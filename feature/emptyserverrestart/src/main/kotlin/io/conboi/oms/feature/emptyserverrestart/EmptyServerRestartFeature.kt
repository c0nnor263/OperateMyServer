package io.conboi.oms.feature.emptyserverrestart

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.common.content.StopManager
import io.conboi.oms.common.foundation.CachedField
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.feature.emptyserverrestart.foundation.reason.EmptyServerRestartStop
import io.conboi.oms.feature.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class EmptyServerRestartFeature(featureInfo: FeatureInfo) :
    OmsFeature<CEmptyServerRestartFeature>(featureInfo) {

    val countTime: CachedField<String, Duration> = CachedField(
        key = { config.countTime.get() },
        value = {
            TimeFormatter.parseToDurationOrNull(config.countTime.get())
                ?: error("Cannot parse countTime")
        }
    )

    private var emptyServerTime: ZonedDateTime? = null

    override fun onOmsTick(event: OMSLifecycle.TickingEvent) {
        val server = event.server

        emptyServerTime?.let { time ->
            val now = TimeHelper.currentTime
            val elapsedSec = TimeHelper.secondsBetween(now, time)
            if (elapsedSec >= countTime.get().inWholeSeconds) {
                val elapsedDuration = elapsedSec.toDuration(DurationUnit.SECONDS)
                OperateMyServer.LOGGER.info("No players detected for ${TimeFormatter.formatDuration(elapsedDuration)}")
                StopManager.stop(server, EmptyServerRestartStop)
                clearTime()
            }
        }
    }

    fun initTime() {
        OperateMyServer.LOGGER.debug("EmptyServerRestart timer started")
        emptyServerTime = TimeHelper.currentTime
    }

    fun clearTime() {
        OperateMyServer.LOGGER.debug("EmptyServerRestart timer cleared")
        emptyServerTime = null
    }
}