package io.conboi.oms.watchdogessentials.feature.lowtps.foundation

import net.minecraft.server.MinecraftServer

object TpsCalculator {
    const val DEFAULT_TPS = 20.0

    fun calculateGlobalTps(server: MinecraftServer): Double {
        return calculateGlobalTps(
            serverTickTimes = server.tickTimes,
            levelTpsSamples = LevelTpsProvider.getAllLevelsTps(server)
        )
    }

    // This is an internal function to allow testing the calculation logic without needing a full server instance
    fun calculateGlobalTps(
        serverTickTimes: LongArray,
        levelTpsSamples: List<Double>
    ): Double {
        // We average the server-level TPS together with every readable dimension sample.
        // This keeps the signal simple and avoids letting a single bad world dominate the result.
        val tpsSamples = buildList {
            add(calculateTps(serverTickTimes))
            addAll(levelTpsSamples)
        }
        return tpsSamples.average()
    }

    fun calculateTps(tickTimes: LongArray): Double {
        if (tickTimes.isEmpty()) return DEFAULT_TPS
        val tickTimeMillis = tickTimes.average() / 1_000_000.0
        if (tickTimeMillis <= 0.0) return DEFAULT_TPS
        return minOf(1000.0 / tickTimeMillis, DEFAULT_TPS)
    }
}