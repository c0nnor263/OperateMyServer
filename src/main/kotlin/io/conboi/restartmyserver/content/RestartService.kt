package io.conboi.restartmyserver.content

import io.conboi.restartmyserver.RestartMyServer
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import java.io.File
import java.nio.charset.StandardCharsets

object RestartService {
    fun restart(server: MinecraftServer, reason: String) {
        try {
            File(server.serverDirectory, "restart_reason.txt")
                .writeText(reason, StandardCharsets.UTF_8)
        } catch (t: Throwable) {
            RestartMyServer.LOGGER.warn("Failed to write restart_reason.txt", t)
        }
        server.playerList.broadcastSystemMessage(Component.literal("§cРестарт: $reason"), false)
//        server.halt(false)
    }
}