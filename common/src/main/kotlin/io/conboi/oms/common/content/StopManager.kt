package io.conboi.oms.common.content

import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.foundation.reason.CrashStop
import io.conboi.oms.common.foundation.reason.StopReason
import io.conboi.oms.common.infrastructure.OMSJson
import io.conboi.oms.common.infrastructure.file.FileUtil
import io.conboi.oms.common.infrastructure.file.OMSPaths
import io.conboi.oms.common.infrastructure.file.StopEntry
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import java.util.*

object StopManager {
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
            }, this::class.java.name + " ShutdownHook")
        )
    }

    fun stop(server: MinecraftServer, reason: StopReason) {
        writeReason(reason)

        server.playerList.broadcastSystemMessage(Component.translatable(reason.messageId), false)
        server.halt(false)
    }

    fun writeReason(reason: StopReason) {
        explicitStopReason = reason
        val entry = StopEntry(reason.name.uppercase(Locale.getDefault()), TimeHelper.currentTime.toString())
        val content = OMSJson.encodeToString(StopEntry.serializer(), entry)
        FileUtil.writeSafe(OMSPaths.stopCause(), content)
    }
}