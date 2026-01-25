package io.conboi.oms.addon.bundled.scheduledrestart

import io.conboi.oms.addon.bundled.scheduledrestart.content.SkipResult
import io.conboi.oms.addon.bundled.scheduledrestart.elements.commands.ScheduledRestartFeatureSkipCommand
import io.conboi.oms.addon.bundled.scheduledrestart.foundation.reason.ScheduledStop
import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.cachedField
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.infrastructure.log.LOG
import io.conboi.oms.common.text.ComponentStyles.bold
import io.conboi.oms.common.text.ComponentStyles.literal
import java.time.LocalTime
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class ScheduledRestartFeature(
    configProvider: ConfigProvider<CScheduledRestartFeature>
) : OmsFeature<CScheduledRestartFeature>(configProvider) {
    companion object Companion {
        const val SKIP_OFFSET_SECONDS = 10L
    }

    override val additionalCommands: List<OMSCommandEntry> = listOf(
        ScheduledRestartFeatureSkipCommand(this)
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
            markConfigAsDirty()
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
        key = { "" }
        value = ::getRestartTime
    }

    val nextRestartTimeTarget = cachedField {
        key = { restartTimeTarget.get() }
        value = {
            val futureTarget = restartTimeTarget.get()
            getRestartTime(futureTarget + SKIP_OFFSET_SECONDS)
        }
    }

    override fun info(): FeatureInfo {
        return super.info().copy(
            id = CScheduledRestartFeature.NAME,
            priority = Priority.COMMON,
            data = hashMapOf(
                "restart_time" to TimeFormatter.formatDateTime(restartTimeTarget.getSnapshotSafely() ?: 0)
            )
        )
    }

    override fun onOmsTick(event: OMSLifecycle.TickingEvent, context: AddonContext) {
        super.onOmsTick(event, context)
        val server = event.server
        val now = TimeHelper.currentTime
        val restartTime = if (isScheduledToSkip) {
            nextRestartTimeTarget.get()
        } else {
            restartTimeTarget.get()
        }
        val remainingSec = TimeHelper.secondsBetween(now, restartTime)
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
        event.server.playerList.broadcastSystemMessage(
            Component.translatable(
                key,
                closestRestartTime.literal().bold()
            ),
            false
        )
    }

    fun handleWarnings(remainingSec: Long, server: MinecraftServer) {
        warningTimes.get().forEach { duration ->
            if (remainingSec == duration.inWholeSeconds) {
                server.playerList.broadcastSystemMessage(
                    Component.translatable(
                        "oms.warning.restart",
                        TimeFormatter.formatDuration(duration).literal().bold()
                    ),
                    false
                )
            }
        }
    }

    fun handleRestart(remainingSec: Long, server: MinecraftServer) {
        if (remainingSec < 0) {
            FORGE_BUS.post(OMSActions.StopRequestedEvent(server, ScheduledStop))
        }
    }

    fun getRestartTime(currentTime: Long = TimeHelper.currentTime): Long {
        val restartTimes = restartTimes.get()
        return pickClosestTarget(restartTimes, currentTime).also {
            LOG.debug("Restart time is set to {}", TimeFormatter.formatDateTime(it))
        }
    }

    fun pickClosestTarget(times: List<LocalTime>, nowEpoch: Long): Long {
        return TimeHelper.closest(nowEpoch, times)
    }


    fun skip(): SkipResult {
        val current = restartTimeTarget.get()
        val nextRestartTime = nextRestartTimeTarget.get()
        return if (isScheduledToSkip) {
            SkipResult.AlreadySkipped(nextRestartTime = nextRestartTime)
        } else {
            isScheduledToSkip = true
            SkipResult.Skipped(skippedRestartTime = current, nextRestartTime = nextRestartTime)
        }
    }
}