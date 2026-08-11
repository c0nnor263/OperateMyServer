package io.conboi.oms.content

import io.conboi.oms.OmsAddons
import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.foundation.reason.CrashStop
import io.conboi.oms.common.infrastructure.OMSJson
import io.conboi.oms.common.infrastructure.file.FileUtil
import io.conboi.oms.infrastructure.config.OMSConfigs
import io.conboi.oms.infrastructure.file.StopEntryLog
import io.conboi.oms.infrastructure.log.AddonLoggerRegistry
import io.conboi.oms.oms
import java.nio.file.Path
import net.minecraft.network.chat.Component

internal object StopManager {
    const val HOOK_NAME = "StopManagerShutdownHook"
    @Volatile
    private var explicitStopReason: StopReason? = null
    private var logStopReasonToFileEnabled: Boolean = false

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

    fun stop(event: OMSActions.StopRequestedEvent) {
        val (server, reason) = event
        writeReason(reason)

        server.playerList.broadcastSystemMessage(Component.translatable(reason.messageId, *reason.arguments), false)
        server.halt(false)
    }

    fun writeReason(reason: StopReason) {
        explicitStopReason = reason
        val reasonName = reason.name.uppercase()
        val reasonMessage = Component.translatable(reason.messageId, *reason.arguments).string
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
        val stopCauseFile: Path = paths.common.resolve("stop_cause.json")

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