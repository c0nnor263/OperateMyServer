package io.conboi.oms.feature.emptyserverrestart

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.common.foundation.CachedField
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.infrastructure.LOG
import io.conboi.oms.feature.emptyserverrestart.foundation.reason.EmptyServerRestartStop
import io.conboi.oms.feature.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

internal class EmptyServerRestartFeature : OmsFeature<CEmptyServerRestartFeature>() {

    override val info: FeatureInfo = FeatureInfo(
        id = CEmptyServerRestartFeature.NAME,
        priority = FeatureInfo.Priority.COMMON
    )

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
            println("Empty server time: $time")
            val now = TimeHelper.currentTime
            val elapsedSec = TimeHelper.secondsBetween(time, now)
            println("Elapsed seconds: $elapsedSec, Count time seconds: ${countTime.get().inWholeSeconds}")
            if (elapsedSec >= countTime.get().inWholeSeconds) {
                val elapsedDuration = elapsedSec.toDuration(DurationUnit.SECONDS)
                LOG.info("No players detected for ${TimeFormatter.formatDuration(elapsedDuration)}")
                FORGE_BUS.post(OMSLifecycle.StopRequestedEvent(server, EmptyServerRestartStop))
                clearTime()
            }
        }
    }

    fun initTime() {
        LOG.debug("EmptyServerRestart timer started")
        emptyServerTime = TimeHelper.currentTime
    }

    fun clearTime() {
        LOG.debug("EmptyServerRestart timer cleared")
        emptyServerTime = null
    }
}