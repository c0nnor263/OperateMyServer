package io.conboi.operatemyserver.common.content

import io.conboi.operatemyserver.common.foundation.StopState
import io.conboi.operatemyserver.common.infrastructure.file.StopLog
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

object StopManager {
    private var explicitStopState: StopState? = null

    fun isServerStopping(): Boolean {
        return explicitStopState != null
    }

    fun installHook() {
        Runtime.getRuntime().addShutdownHook(
            Thread({
                if (explicitStopState == null) {
                    StopLog().write(StopState.CRASH)
                }
            }, this::class.java.name + " ShutdownHook")
        )
    }

    fun stop(server: MinecraftServer, reason: StopState, force: Boolean = true) {
        writeReason(reason)

        server.playerList.broadcastSystemMessage(Component.literal("Restarting server... ($reason)"), false)
        server.halt(false)
    }

    fun writeReason(reason: StopState) {
        explicitStopState = reason
        StopLog().write(reason)
    }
}