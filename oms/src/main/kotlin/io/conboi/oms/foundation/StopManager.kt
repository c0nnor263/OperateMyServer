package io.conboi.oms.foundation

import io.conboi.oms.OmsAddons
import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.TickTimer
import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.foundation.reason.CrashStop
import io.conboi.oms.common.infrastructure.OMSJson
import io.conboi.oms.common.infrastructure.file.FileUtil
import io.conboi.oms.common.infrastructure.lang.OmsLang
import io.conboi.oms.common.infrastructure.log.LOG
import io.conboi.oms.infrastructure.config.OMSConfigs
import io.conboi.oms.infrastructure.file.StopEntryLog
import io.conboi.oms.infrastructure.log.AddonLoggerRegistry
import io.conboi.oms.oms
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

internal object StopManager {
    const val HOOK_NAME = "StopManagerShutdownHook"
    val SERVER_HALT_DELAY = 3.seconds

    @Volatile
    private var explicitStopReason: StopReason? = null
    private var logStopReasonToFileEnabled: Boolean = false
    private var isStopScheduled: Boolean = false
    private const val STOP_CAUSE_FILE_NAME = "stop_cause.json"
    private val tickTimer = TickTimer(SERVER_HALT_DELAY.inWholeSeconds.toInt() * 20)

    fun isServerStopping(): Boolean {
        return explicitStopReason != null
    }

    fun installHook() {
        logStopReasonToFileEnabled = OMSConfigs.server.common.logStopReasonToFile.get()
        Runtime.getRuntime().addShutdownHook(
            Thread({
                if (explicitStopReason == null) {
                    writeReason(CrashStop)
                }
            }, HOOK_NAME)
        )
    }

    fun onOmsTick(event: OMSLifecycle.TickingEvent) {
        if (!isStopScheduled) return
        val server = event.server
        if (!tickTimer.shouldFire(server.tickCount)) return
        if (server.isRunning) {
            server.halt(false)
        }
    }

    fun scheduleStop(event: OMSActions.StopRequestedEvent) {
        if (isStopScheduled) return
        val (server, reason) = event
        writeReason(reason)

        LOG.info(
            "Stopping server due to reason: ${reason.name.uppercase()} - ${
                OmsLang.translatable(
                    reason.messageId,
                    *reason.arguments
                ).string
            }"
        )
        server.playerList.broadcastSystemMessage(OmsLang.translatable(reason.messageId, *reason.arguments), false)
        isStopScheduled = true
    }

    fun writeReason(reason: StopReason) {
        explicitStopReason = reason
        val reasonName = reason.name.uppercase()
        val reasonMessage = OmsLang.translatable(reason.messageId, *reason.arguments).string
        val time = TimeFormatter.formatDateTime(TimeHelper.currentEpochSeconds)
        val entry = StopEntryLog(
            reason = reasonName,
            message = reasonMessage,
            shouldRestart = reason.shouldRestart,
            time = time
        )
        val content = OMSJson.encodeToString(StopEntryLog.serializer(), entry)
        val context = OmsAddons.oms.context
        val paths = context.paths
        val stopCauseFile: Path = paths.common.resolve(STOP_CAUSE_FILE_NAME)

        FileUtil.writeSafe(stopCauseFile, content)
        if (logStopReasonToFileEnabled) {
            val logger = AddonLoggerRegistry.persistent("restart", { paths.logs })
            logger.info(
                "Server stopping due to reason: $reasonName - $reasonMessage\n" +
                        "Stop cause written to: $stopCauseFile\n" +
                        "Should restart: ${reason.shouldRestart}\n" +
                        "Time: $time"
            )
        }
    }
}