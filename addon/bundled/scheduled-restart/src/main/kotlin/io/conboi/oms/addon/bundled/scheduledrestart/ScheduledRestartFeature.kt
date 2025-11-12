package io.conboi.oms.addon.bundled.scheduledrestart

import io.conboi.oms.addon.bundled.scheduledrestart.content.SkipResult
import io.conboi.oms.addon.bundled.scheduledrestart.elements.commands.ScheduledRestartFeatureSkipCommand
import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.cachedField
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.core.foundation.reason.ScheduledStop
import io.conboi.oms.core.infrastructure.LOG
import io.conboi.oms.utils.foundation.TimeFormatter
import io.conboi.oms.utils.foundation.TimeHelper
import java.time.LocalTime
import java.time.ZonedDateTime
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

internal class ScheduledRestartFeature : OmsFeature<CScheduledRestartFeature>() {
    companion object Companion {
        const val SKIP_OFFSET_SECONDS = 10L
        const val MAX_DAYS_FOR_LOOKAHEAD = 1L
    }

    override val info: FeatureInfo = FeatureInfo(
        id = CScheduledRestartFeature.NAME,
        priority = FeatureInfo.Priority.COMMON
    )

    private var isScheduledToSkip = false

    val restartTimes = configField {
        key = { config.restartTimes.get() }
        value = {
            config.restartTimes.get()
                .mapNotNull(TimeFormatter::parseToLocalTimeOrNull)
                .sortedBy { it.toSecondOfDay() }
        }
        validator = { list ->
            list.isNotEmpty()
        }
        onUpdate = { _, _ ->
            flagConfigAsDirty()
        }
    }

    val warningTimes = configField {
        key = { config.warningTimes.get() }
        value = {
            config.warningTimes.get()
                .mapNotNull(TimeFormatter::parseToDurationOrNull)
                .sortedByDescending { it.inWholeSeconds }
        }
        validator = { list ->
            list.isNotEmpty()
        }
    }

    val restartTimeTarget = cachedField {
        key = { isScheduledToSkip }
        value = ::initRestartTimeTarget
    }

    override fun getFeatureCommands(): List<OMSCommandEntry> = listOf(
        ScheduledRestartFeatureSkipCommand()
    )

    override fun onOmsTick(event: OMSLifecycle.TickingEvent) {
        super.onOmsTick(event)
        val server = event.server
        val now = TimeHelper.currentTime
        val remainingSec = TimeHelper.secondsBetween(now, restartTimeTarget.get())
        handleWarnings(remainingSec, server)
        handleRestart(remainingSec, server)
    }

    override fun onConfigUpdated(event: OMSLifecycle.TickingEvent) {
        super.onConfigUpdated(event)
        val key = if (isScheduledToSkip) {
            isScheduledToSkip = false
            "oms.warning.autorestart.config_updated_skip_reset"
        } else {
            "oms.warning.autorestart.config_updated"
        }
        restartTimeTarget.invalidate()
        val closestRestartTime = TimeFormatter.formatDateTime(restartTimeTarget.get())
        event.server.playerList.broadcastSystemMessage(Component.translatable(key, closestRestartTime), false)
    }

    fun handleWarnings(remainingSec: Long, server: MinecraftServer) {
        if (isScheduledToSkip) return
        warningTimes.get().forEach { duration ->
            if (remainingSec == duration.inWholeSeconds) {
                server.playerList.broadcastSystemMessage(
                    Component.translatable("oms.warning.restart", TimeFormatter.formatDuration(duration)),
                    false
                )
            }
        }
    }

    fun handleRestart(remainingSec: Long, server: MinecraftServer) {
        if (remainingSec < 0) {
            if (isScheduledToSkip) {
                isScheduledToSkip = false
                return
            }
            FORGE_BUS.post(OMSLifecycle.StopRequestedEvent(server, ScheduledStop))
        }
    }

    fun initRestartTimeTarget(): ZonedDateTime {
        val restartTimes = restartTimes.get()
        return pickClosestTarget(restartTimes).also {
            LOG.debug("Next restart time is set to {}", it)
        }
    }

    fun pickClosestTarget(
        times: List<LocalTime>,
        now: ZonedDateTime = TimeHelper.currentTime,
        maxDaysLookahead: Long = MAX_DAYS_FOR_LOOKAHEAD
    ): ZonedDateTime {
        val candidates = times.map { time ->
            TimeHelper.convertLocalTimeToZonedDateTime(now, time)
        }
        val closestTime = TimeHelper.closest(now, candidates)
        return if (closestTime == null && maxDaysLookahead > 0) {
            val tomorrow = now
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
            pickClosestTarget(times, tomorrow, maxDaysLookahead - 1)
        } else {
            closestTime
        } ?: error("cannot pick closest target")
    }

    fun getNextRestartTime(): ZonedDateTime {
        val futureTarget = restartTimeTarget.get().plusSeconds(SKIP_OFFSET_SECONDS)
        return pickClosestTarget(restartTimes.get(), futureTarget)
    }

    fun skip(): SkipResult {
        val current = restartTimeTarget.get()
        val next = getNextRestartTime()
        return if (isScheduledToSkip) {
            SkipResult.AlreadySkipped(next = next)
        } else {
            isScheduledToSkip = true
            SkipResult.Skipped(skipped = current, next = next)
        }
    }
}