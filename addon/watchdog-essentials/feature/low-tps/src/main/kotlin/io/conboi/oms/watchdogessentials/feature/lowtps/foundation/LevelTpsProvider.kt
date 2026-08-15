package io.conboi.oms.watchdogessentials.feature.lowtps.foundation

import net.minecraft.server.MinecraftServer

object LevelTpsProvider {

    fun getAllLevelsTps(server: MinecraftServer): List<Double> {
        return getAllLevelsTps(levelTickTimes = server.allLevels.map { LevelTickTimeProvider.getTickTimes(it) })
    }

    fun getAllLevelsTps(levelTickTimes: Iterable<LongArray?>): List<Double> {
        return levelTickTimes.mapNotNull { times ->
            // Some dimensions may not expose tick timing yet; skip those instead of failing the whole sample.
            times?.let(TpsCalculator::calculateTps)
        }
    }
}