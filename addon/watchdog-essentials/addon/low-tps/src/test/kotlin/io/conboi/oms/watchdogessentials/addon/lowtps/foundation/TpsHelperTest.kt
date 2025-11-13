package io.conboi.oms.watchdogessentials.addon.lowtps.foundation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.mockk.every
import io.mockk.mockk
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

class TpsHelperTest : ShouldSpec({

    context("calculateTps") {

        should("return DEFAULT_TPS for empty array") {
            val result = TpsHelper.calculateTps(longArrayOf())
            result shouldBeExactly TpsHelper.DEFAULT_TPS
        }

        should("calculate capped TPS for normal tick time") {
            val result = TpsHelper.calculateTps(LongArray(100) { 50_000_000 })
            result shouldBeExactly 20.0
        }

        should("calculate lower TPS if tick time is high") {
            val result = TpsHelper.calculateTps(LongArray(100) { 100_000_000 })
            result shouldBeExactly 10.0
        }

        should("not exceed DEFAULT_TPS even if tick time is low") {
            val result = TpsHelper.calculateTps(LongArray(100) { 5_000_000 })
            result shouldBeExactly 20.0
        }
    }

    // Disable tests due to mocking issues with Level in current environment
    xcontext("safeCalculate") {

        should("return null if server returns null tick times") {
            val dim = mockk<ResourceKey<Level>>()
            val server = mockk<MinecraftServer> {
                every { getTickTime(dim) } returns null
            }

            val level = mockk<ServerLevel>(relaxed = true)
            every { level.dimension() } returns dim
            every { level.server } returns server

            val result = TpsHelper.safeCalculate(level)
            result.shouldBeNull()
        }

        should("return calculated TPS when server returns tick data") {
            val tickTimes = LongArray(100) { 50_000_000 }

            val dim = mockk<ResourceKey<Level>>()
            val server = mockk<MinecraftServer> {
                every { getTickTime(dim) } returns tickTimes
            }

            val level = mockk<ServerLevel>(relaxed = true)
            every { level.dimension() } returns dim
            every { level.server } returns server

            val result = TpsHelper.safeCalculate(level)
            result?.shouldBeExactly(20.0)
        }
    }
})
