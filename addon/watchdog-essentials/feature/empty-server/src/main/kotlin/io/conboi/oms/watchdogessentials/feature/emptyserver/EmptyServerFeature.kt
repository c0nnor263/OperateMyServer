package io.conboi.oms.watchdogessentials.feature.emptyserver

import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import io.conboi.oms.watchdogessentials.feature.emptyserver.event.EmptyServerEvents
import io.conboi.oms.watchdogessentials.feature.emptyserver.foundation.reason.EmptyServerStop
import io.conboi.oms.watchdogessentials.feature.emptyserver.infrastructure.config.CEmptyServerFeature
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class EmptyServerFeature(
    configProvider: ConfigProvider<CEmptyServerFeature>
) : OmsFeature<CEmptyServerFeature>(configProvider) {

    override fun createEventListeners(): List<Any> {
        return listOf(
            EmptyServerEvents(this)
        )
    }

    override fun info(): FeatureInfo {
        return super.info().copy(
            id = CEmptyServerFeature.NAME,
            priority = Priority.COMMON,
        )
    }

    val countTime = configField {
        key = { config.countTime.get() }
        value = {
            TimeFormatter.parseToDurationOrNull(config.countTime.get())
                ?: error("Cannot parse countTime")
        }
    }

    private var emptyServerTime: Long? = null

    override fun onOmsTick(event: OMSLifecycle.TickingEvent, context: AddonContext) {
        super.onOmsTick(event, context)
        val server = event.server
        val time = emptyServerTime ?: return
        val now = TimeHelper.currentTime
        val elapsedSec = TimeHelper.secondsBetween(time, now)
        if (elapsedSec >= countTime.get().inWholeSeconds) {
            val elapsedDuration = elapsedSec.toDuration(DurationUnit.SECONDS)
            LOG.info("No players detected for ${TimeFormatter.formatDuration(elapsedDuration)}")
            FORGE_BUS.post(OMSActions.StopRequestedEvent(server, EmptyServerStop))
            clearTime()
        }
    }

    fun initTime() {
        LOG.debug("EmptyServer timer started")
        emptyServerTime = TimeHelper.currentTime
    }

    fun clearTime() {
        LOG.debug("EmptyServer timer cleared")
        emptyServerTime = null
    }
}