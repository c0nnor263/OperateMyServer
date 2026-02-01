package io.conboi.oms.watchdogessentials.addon.emptyserverrestart

import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.event.EmptyServerRestartEvents
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.reason.EmptyServerRestartStop
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class EmptyServerRestartFeature(
    configProvider: ConfigProvider<CEmptyServerRestartFeature>
) : OmsFeature<CEmptyServerRestartFeature>(configProvider) {

    override fun createEventListeners(): List<Any> {
        return listOf(
            EmptyServerRestartEvents(this)
        )
    }

    override fun info(): FeatureInfo {
        return super.info().copy(
            id = CEmptyServerRestartFeature.NAME,
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
            FORGE_BUS.post(OMSActions.StopRequestedEvent(server, EmptyServerRestartStop))
            clearTime()
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