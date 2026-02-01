package io.conboi.oms.watchdogessentials.addon.lowtps.foundation

import net.minecraft.server.level.ServerLevel

internal object TpsHelper {
    const val DEFAULT_TPS = 20.0

    fun calculateTps(tickTimes: LongArray): Double {
        if (tickTimes.isEmpty()) return DEFAULT_TPS
        val tickTime = tickTimes.average() / 1_000_000.0
        return minOf(1000.0 / tickTime, DEFAULT_TPS)
    }

    fun safeCalculate(level: ServerLevel): Double? {
        val times = level.server.getTickTime(level.dimension()) ?: return null
        return calculateTps(times)
    }
}