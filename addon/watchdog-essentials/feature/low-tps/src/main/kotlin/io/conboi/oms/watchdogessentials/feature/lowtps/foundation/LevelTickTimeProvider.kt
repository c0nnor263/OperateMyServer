package io.conboi.oms.watchdogessentials.feature.lowtps.foundation

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

object LevelTickTimeProvider {
    fun getTickTimes(level: ServerLevel): LongArray? {
        return getTickTimes(level.server, level.dimension())
    }

    fun getTickTimes(
        server: MinecraftServer,
        dimension: ResourceKey<Level>
    ): LongArray? {
        return server.getTickTime(dimension)
    }
}
