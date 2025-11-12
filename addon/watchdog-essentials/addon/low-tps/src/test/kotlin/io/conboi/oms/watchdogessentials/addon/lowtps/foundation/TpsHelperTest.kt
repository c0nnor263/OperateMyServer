package io.conboi.oms.watchdogessentials.addon.lowtps.foundation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.shouldBeExactly

class TpsHelperTest : FunSpec({

    context("calculateTps") {

        test("should return DEFAULT_TPS for empty array") {
            val result = TpsHelper.calculateTps(longArrayOf())
            result shouldBeExactly TpsHelper.DEFAULT_TPS
        }

        test("should calculate capped TPS for normal tick time") {
            val result = TpsHelper.calculateTps(LongArray(100) { 50_000_000 })
            result shouldBeExactly 20.0
        }

        test("should calculate lower TPS if tick time is high") {
            val result = TpsHelper.calculateTps(LongArray(100) { 100_000_000 })
            result shouldBeExactly 10.0
        }

        test("should not exceed DEFAULT_TPS even if tick time is low") {
            val result = TpsHelper.calculateTps(LongArray(100) { 5_000_000 })
            result shouldBeExactly 20.0
        }
    }

    // TODO: Currently cannot mock final classes like MinecraftServer and ServerLevel easily
//    context("safeCalculate") {
//
//        test("should return null if getTickTime returns null") {
//            val server = mockk<MinecraftServer> {
//                every { getTickTime(any()) } returns null
//            }
//
//            val level = mockk<ServerLevel>(relaxed = true)
//            every { level.dimension() } returns mockk()
//            every { level.server } returns server
//
//            val result = TpsHelper.safeCalculate(level)
//            result.shouldBeNull()
//        }
//
//        test("should return calculated TPS if getTickTime returns data") {
//            val tickTimes = LongArray(100) { 50_000_000 }
//
//            val dummyDimension = mockk<ResourceKey<Level>>()
//            val server = mockk<MinecraftServer> {
//                every { getTickTime(dummyDimension) } returns tickTimes
//            }
//
//            val level = mockk<ServerLevel>(relaxed = true)
//            every { level.dimension() } returns dummyDimension
//            every { level.server } returns server
//
//            val result = TpsHelper.safeCalculate(level)
//            result?.shouldBeExactly(20.0)
//        }
//    }
})