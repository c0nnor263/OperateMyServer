package io.conboi.oms.watchdogessentials.feature.lowtps.foundation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level

class LevelTickTimeProviderTest : ShouldSpec({

    should("return tick times from the server for a dimension") {
        val server = mockk<MinecraftServer>()
        val dimension = mockk<ResourceKey<Level>>()
        val tickTimes = LongArray(3) { 42L }

        every { server.getTickTime(dimension) } returns tickTimes

        LevelTickTimeProvider.getTickTimes(server, dimension) shouldBe tickTimes
    }
})
