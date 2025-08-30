package io.conboi.operatemyserver.feature.lowtps.foundation

import io.conboi.operatemyserver.common.foundation.TimeHelper
import net.minecraft.server.MinecraftServer
import kotlin.time.Duration

object TpsMonitor {
    private val history = ArrayDeque<TpsSnapshot>()
    const val MAX_RETENTION_MINUTES = 5L
    const val MIN_RETENTION_MINUTES = 1L

    fun update(server: MinecraftServer) {
        val now = TimeHelper.currentTime
        history.addLast(TpsSnapshot(now, calculateGlobalTps(server)))
        val cutoff = now.minusMinutes(MAX_RETENTION_MINUTES)
        while (history.isNotEmpty() && history.first().time < cutoff) {
            history.removeFirst()
        }
    }

    fun averageTpsOver(tpsCountTime: Duration): Double {
        val minutes = tpsCountTime.inWholeMinutes
        val cutoff = TimeHelper.currentTime.minusMinutes(minutes)
        val values = history.filter { it.time >= cutoff }.map { it.value }

        // Ensure we have enough data points and the requested duration is reasonable
        if (values.isEmpty() || minutes < MIN_RETENTION_MINUTES) {
            return 20.0
        }

        return values.average()
    }

    private fun calculateGlobalTps(server: MinecraftServer): Double {
        val serverTicks = TpsHelper.calculateTps(server.tickTimes)
        val dimensionTps = server.allLevels
            .mapNotNull { TpsHelper.safeCalculate(it) }
        return (listOf(serverTicks) + dimensionTps).average()
    }
}