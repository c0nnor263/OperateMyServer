package io.conboi.oms.content

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.core.foundation.reason.CrashStop
import io.conboi.oms.core.infrastructure.file.OMSPaths
import io.conboi.oms.infrastructure.file.StopEntryLog
import io.conboi.oms.utils.foundation.TimeHelper
import io.conboi.oms.utils.infrastructure.OMSJson
import io.conboi.oms.utils.infrastructure.file.FileUtil
import net.minecraft.network.chat.Component
import org.jetbrains.annotations.VisibleForTesting

internal object StopManager {
    const val HOOK_NAME = "StopManagerShutdownHook"
    private var explicitStopReason: StopReason? = null

    fun isServerStopping(): Boolean {
        return explicitStopReason != null
    }

    fun installHook() {
        Runtime.getRuntime().addShutdownHook(
            Thread({
                if (explicitStopReason == null) {
                    writeReason(CrashStop)
                }
            }, HOOK_NAME)
        )
    }

    fun stop(event: OMSLifecycle.StopRequestedEvent) {
        val (server, reason) = event
        writeReason(reason)

        server.playerList.broadcastSystemMessage(Component.translatable(reason.messageId), false)
        server.halt(false)
    }

    fun writeReason(reason: StopReason) {
        explicitStopReason = reason
        val reasonName = reason.name.uppercase()
        val reasonMessage = Component.translatable(reason.messageId).string

        val time = TimeHelper.currentTime.toString()
        val entry = StopEntryLog(reasonName, reasonMessage, time)
        val content = OMSJson.encodeToString(StopEntryLog.serializer(), entry)
        FileUtil.writeSafe(OMSPaths.stopCause(), content)
    }

    @VisibleForTesting
    fun clearReason() {
        explicitStopReason = null
    }
}