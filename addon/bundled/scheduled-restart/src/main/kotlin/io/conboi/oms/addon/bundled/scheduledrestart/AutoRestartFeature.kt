package io.conboi.oms.addon.bundled.scheduledrestart

import io.conboi.oms.addon.bundled.scheduledrestart.content.SkipResult
import io.conboi.oms.addon.bundled.scheduledrestart.elements.commands.AutoRestartFeatureSkipCommand
import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CAutoRestartFeature
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.core.foundation.reason.ScheduledStop
import io.conboi.oms.core.infrastructure.LOG
import io.conboi.oms.utils.foundation.CachedField
import io.conboi.oms.utils.foundation.TimeFormatter
import io.conboi.oms.utils.foundation.TimeHelper
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.time.Duration
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

internal class AutoRestartFeature : OmsFeature<CAutoRestartFeature>() {
    companion object {
        const val SKIP_OFFSET_SECONDS = 10L
        const val MAX_DAYS_FOR_LOOKAHEAD = 1L
    }

    override val info: FeatureInfo = FeatureInfo(
        id = CAutoRestartFeature.NAME,
        priority = FeatureInfo.Priority.COMMON
    )

    private var isScheduledToSkip = false

    // TODO: Maybe move to OmsFeature?
    private var isConfigurationUpdated = false

    val restartTimes: CachedField<List<String>, List<LocalTime>> =
        CachedField(
            key = { config.restartTimes.get() },
            value = {
                config.restartTimes.get()
                    .mapNotNull(TimeFormatter::parseToLocalTimeOrNull)
                    .sortedBy { it.toSecondOfDay() }
            },
            valueValidator = { list ->
                list.isNotEmpty()
            },
            onUpdate = { _, _ ->
                isConfigurationUpdated = true
            }
        )


    val warningTimes: CachedField<List<String>, List<Duration>> =
        CachedField(
            key = { config.warningTimes.get() },
            value = {
                config.warningTimes.get()
                    .mapNotNull(TimeFormatter::parseToDurationOrNull)
                    .sortedByDescending { it.inWholeSeconds }
            },
            valueValidator = { list ->
                list.isNotEmpty()
            },
        )

    val restartTimeTarget: CachedField<Boolean, ZonedDateTime> =
        CachedField(
            key = { isScheduledToSkip },
            value = ::initRestartTimeTarget
        )

    override fun getFeatureCommands(): List<OMSCommandEntry> = listOf(
        AutoRestartFeatureSkipCommand()
    )

    override fun onOmsTick(event: OMSLifecycle.TickingEvent) {
        val server = event.server
        val now = TimeHelper.currentTime
        val remainingSec = TimeHelper.secondsBetween(now, restartTimeTarget.get())
        checkIfConfigurationUpdated(server)
        checkWarningTimes(remainingSec, server)
        checkForRestart(remainingSec, server)
    }

    private fun checkIfConfigurationUpdated(
        server: MinecraftServer
    ) {
        if (!isConfigurationUpdated) return
        isConfigurationUpdated = false

        if (isScheduledToSkip) {
            isScheduledToSkip = false
            restartTimeTarget.invalidate()
            server.playerList.broadcastSystemMessage(
                Component.translatable("oms.warning.autorestart.config_updated_skip_reset"),
                false
            )
        } else {
            server.playerList.broadcastSystemMessage(
                Component.translatable("oms.warning.autorestart.config_updated"),
                false
            )
        }
    }

    private fun initRestartTimeTarget(): ZonedDateTime {
        val restartTimes = restartTimes.get()
        return pickClosestTarget(restartTimes).also {
            LOG.debug("Next restart time is set to {}", it)
        }
    }

    private fun pickClosestTarget(
        times: List<LocalTime>,
        now: ZonedDateTime = TimeHelper.currentTime,
        maxDaysLookahead: Long = MAX_DAYS_FOR_LOOKAHEAD
    ): ZonedDateTime {
        val candidates = times.map { time ->
            TimeHelper.convertLocalTimeToZonedDateTime(now, time)
        }
        val closestTime = TimeHelper.closest(now, candidates)
        return if (closestTime == null && maxDaysLookahead > 0) {
            val tomorrow = now.plusDays(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0)
            pickClosestTarget(times, tomorrow, maxDaysLookahead - 1)
        } else {
            closestTime
        } ?: error("cannot pick closest target")
    }

    private fun checkWarningTimes(remainingSec: Long, server: MinecraftServer) {
        if (isScheduledToSkip) return
        warningTimes.get().forEach { duration ->
            val seconds = duration.inWholeSeconds
            if (remainingSec == seconds) {
                sendWarning(server, duration)
            }
        }
    }

    private fun sendWarning(server: MinecraftServer, duration: Duration) {
        server.playerList.broadcastSystemMessage(
            Component.translatable("oms.warning.restart", TimeFormatter.formatDuration(duration)),
            false
        )
    }


    private fun checkForRestart(remainingSec: Long, server: MinecraftServer) {
        if (remainingSec < 0) {
            if (isScheduledToSkip) {
                isScheduledToSkip = false
                return
            }
            FORGE_BUS.post(OMSLifecycle.StopRequestedEvent(server, ScheduledStop))
        }
    }

    private fun getNextRestartTime(): ZonedDateTime {
        val futureTarget = restartTimeTarget.get().plusSeconds(SKIP_OFFSET_SECONDS)
        return pickClosestTarget(restartTimes.get(), futureTarget)
    }

    internal fun skip(): SkipResult {
        val current = restartTimeTarget.get()
        val nextTarget = getNextRestartTime()
        if (isScheduledToSkip) {
            return SkipResult.AlreadySkipped(next = nextTarget)
        }

        isScheduledToSkip = true
        return SkipResult.Skipped(skipped = current, next = nextTarget)
    }
}