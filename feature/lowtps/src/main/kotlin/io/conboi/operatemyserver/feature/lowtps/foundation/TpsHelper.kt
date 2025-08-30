package io.conboi.operatemyserver.feature.lowtps.foundation

import net.minecraft.server.level.ServerLevel

object TpsHelper {
    fun calculateTps(tickTimes: LongArray): Double {
        if (tickTimes.isEmpty()) return 20.0
        val tickTime = tickTimes.average() / 1_000_000.0
        return minOf(1000.0 / tickTime, 20.0)
    }

    fun safeCalculate(level: ServerLevel): Double? {
        val times = level.server.getTickTime(level.dimension()) ?: return null
        return calculateTps(times)
    }
}