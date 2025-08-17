package io.conboi.restartmyserver.content.feature

import io.conboi.restartmyserver.content.RestartService
import io.conboi.restartmyserver.foundation.feature.FeatureType
import io.conboi.restartmyserver.foundation.feature.RmsFeature
import io.conboi.restartmyserver.infrastructure.CServer
import io.conboi.restartmyserver.infrastructure.feature.CAutoRestartFeature
import io.conboi.restartmyserver.util.TimeFormatter
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration

// TODO Make config static and update it on server event reload config
class AutoRestartFeature(serverConfig: CServer) :
    RmsFeature<CAutoRestartFeature>(serverConfig, FeatureType.AUTO_RESTART) {

    private var nextTarget: ZonedDateTime? = null
    override fun tick(server: MinecraftServer) {
        val times: List<LocalTime> = featureConfig.restartTimes.get()
            .mapNotNull(TimeFormatter::parseToLocalTimeOrNull)
            .sortedBy { it.toSecondOfDay() }
        if (times.isEmpty()) {
            server.playerList.broadcastSystemMessage(
                Component.literal("§cАвто-рестарт не настроен! Проверьте конфиг RMS"),
                false
            )
            return
        }

        // 2) парсим моменты предупреждений (10s/1m/5m/…)
        val warningDurations: List<Duration> = featureConfig.warningTimes.get()
            .mapNotNull(TimeFormatter::parseToDurationOrNull)
            .sortedByDescending { it.inWholeSeconds }
        if (warningDurations.isEmpty()) {
            server.playerList.broadcastSystemMessage(
                Component.literal("§cАвто-рестарт не настроен! Проверьте конфиг RMS"),
                false
            )
            return
        }

        val zone: ZoneId = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)

        // 3) ближайшая цель
        val target = (nextTarget ?: TimeFormatter.pickClosestTarget(now, times)).also { nextTarget = it }

        // 4) оставшееся время
        val remainingSec = TimeFormatter.secondsBetween(now, target)

        // 5) дискретные предупреждения (ровно за N из warningTimes)
        warningDurations.forEach { duration ->
            val seconds = duration.inWholeSeconds
            if (remainingSec == seconds) {
                server.playerList.broadcastSystemMessage(
                    Component.literal("§eПерезапуск через §6${TimeFormatter.formatShort(duration)}§e…"),
                    false
                )
            }
        }

        if (remainingSec <= 0) {
            RestartService.restart(server, "STOP EXCEPTION RESTART")
            nextTarget = null
        }
    }
}