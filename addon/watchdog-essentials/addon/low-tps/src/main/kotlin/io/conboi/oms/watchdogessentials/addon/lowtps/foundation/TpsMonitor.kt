package io.conboi.oms.watchdogessentials.addon.lowtps.foundation

import io.conboi.oms.common.foundation.TimeHelper
import kotlin.time.Duration
import net.minecraft.server.MinecraftServer

internal object TpsMonitor {
    private val history = ArrayDeque<TpsSnapshot>()
    const val MIN_RETENTION_MINUTES = 1L
    const val MAX_RETENTION_MINUTES = 5L

    fun update(server: MinecraftServer) {
        val now = TimeHelper.currentTime
        history.addLast(TpsSnapshot(now, calculateGlobalTps(server)))
        val cutoff = now - MAX_RETENTION_MINUTES
        while (history.isNotEmpty() && history.first().time < cutoff) {
            history.removeFirst()
        }
    }

    fun averageTpsOver(tpsCountTime: Duration): Double {
        val minutes = tpsCountTime.inWholeMinutes
        val cutoff = TimeHelper.currentTime - minutes
        val values = history.filter { it.time >= cutoff }.map { it.value }

        // Ensure we have enough data points and the requested duration is reasonable
        if (values.isEmpty() || minutes < MIN_RETENTION_MINUTES) {
            return TpsHelper.DEFAULT_TPS
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