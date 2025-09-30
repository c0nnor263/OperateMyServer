package io.conboi.oms.common.content

import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.foundation.reason.CrashStop
import io.conboi.oms.common.foundation.reason.StopReason
import io.conboi.oms.common.infrastructure.OMSJson
import io.conboi.oms.common.infrastructure.file.FileUtil
import io.conboi.oms.common.infrastructure.file.OMSPaths
import io.conboi.oms.common.infrastructure.file.StopEntryLog
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import org.jetbrains.annotations.VisibleForTesting

object StopManager {
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

    fun stop(server: MinecraftServer, reason: StopReason) {
        writeReason(reason)

        server.playerList.broadcastSystemMessage(Component.translatable(reason.messageId), false)
        server.halt(false)
    }

    fun writeReason(reason: StopReason) {
        explicitStopReason = reason
        val reasonName = reason.name.uppercase()
        val time = TimeHelper.currentTime.toString()
        val entry = StopEntryLog(reasonName, time)
        val content = OMSJson.encodeToString(StopEntryLog.serializer(), entry)
        FileUtil.writeSafe(OMSPaths.stopCause(), content)
    }

    @VisibleForTesting
    fun clearReason() {
        explicitStopReason = null
    }
}