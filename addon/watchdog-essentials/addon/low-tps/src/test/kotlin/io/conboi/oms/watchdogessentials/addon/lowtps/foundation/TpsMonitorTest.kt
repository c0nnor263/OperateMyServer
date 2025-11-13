package io.conboi.oms.watchdogessentials.addon.lowtps.foundation

import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.ShouldSpec


// Test is ignored because of issues with Level mocking in the current environment
@Ignored
class TpsMonitorTest : ShouldSpec({
//
//    lateinit var server: MinecraftServer
//    lateinit var level1: ServerLevel
//    lateinit var level2: ServerLevel
//
//    val now = ZonedDateTime.parse("2025-01-01T10:00:00Z")
//
//    fun clearHistory() {
//        val field = TpsMonitor::class.java.getDeclaredField("history")
//        field.isAccessible = true
//        (field.get(null) as ArrayDeque<*>).clear()
//    }
//
//    beforeEach {
//        mockkObject(TpsHelper, TimeHelper)
//
//        server = mockk(relaxed = true)
//        level1 = mockk(relaxed = true)
//        level2 = mockk(relaxed = true)
//
//        every { TimeHelper.currentTime } returns now
//        every { server.allLevels } returns listOf(level1, level2)
//
//        clearHistory()
//    }
//
//    afterEach {
//        unmockkAll()
//    }
//
//    context("update") {
//
//        should("add snapshot with global TPS") {
//            every { server.tickTimes } returns LongArray(100) { 50_000_000 }
//            every { TpsHelper.calculateTps(any()) } returns 18.0
//            every { TpsHelper.safeCalculate(any()) } returns 16.0
//
//            TpsMonitor.update(server)
//
//            val field = TpsMonitor::class.java.getDeclaredField("history")
//            field.isAccessible = true
//            val history = field.get(null) as ArrayDeque<TpsSnapshot>
//
//            history.size shouldBe 1
//            history.first().value shouldBeExactly (18.0 + 16.0 + 16.0) / 3
//        }
//
//        should("remove old history entries") {
//            val field = TpsMonitor::class.java.getDeclaredField("history")
//            field.isAccessible = true
//            val history = field.get(null) as ArrayDeque<TpsSnapshot>
//
//            val oldTime = now.minusMinutes(TpsMonitor.MAX_RETENTION_MINUTES + 1)
//            history.addLast(TpsSnapshot(oldTime, 5.0))
//            history.addLast(TpsSnapshot(now, 10.0))
//
//            every { server.tickTimes } returns LongArray(100) { 50_000_000 }
//            every { TpsHelper.calculateTps(any()) } returns 18.0
//            every { TpsHelper.safeCalculate(any()) } returns null
//
//            TpsMonitor.update(server)
//
//            history.size shouldBe 2
//            history.first().time shouldBe now
//        }
//    }
//
//    context("averageTpsOver") {
//
//        should("return DEFAULT_TPS if no values") {
//            val avg = TpsMonitor.averageTpsOver(3.minutes)
//            avg shouldBeExactly TpsHelper.DEFAULT_TPS
//        }
//
//        should("return DEFAULT_TPS if duration < MIN_RETENTION_MINUTES") {
//            val field = TpsMonitor::class.java.getDeclaredField("history")
//            field.isAccessible = true
//            val history = field.get(null) as ArrayDeque<TpsSnapshot>
//
//            history.addLast(TpsSnapshot(now, 5.0))
//
//            val avg = TpsMonitor.averageTpsOver(0.minutes)
//            avg shouldBeExactly TpsHelper.DEFAULT_TPS
//        }
//
//        should("compute correct average for values >= cutoff") {
//            every { TimeHelper.currentTime } returns now
//
//            val field = TpsMonitor::class.java.getDeclaredField("history")
//            field.isAccessible = true
//            val history = field.get(null) as ArrayDeque<TpsSnapshot>
//
//            history.addLast(TpsSnapshot(now.minusMinutes(3), 10.0))
//            history.addLast(TpsSnapshot(now.minusMinutes(2), 20.0))
//            history.addLast(TpsSnapshot(now.minusMinutes(1), 30.0))
//
//            val avg = TpsMonitor.averageTpsOver(3.minutes)
//            avg shouldBeExactly 20.0
//        }
//    }
//
//    context("global TPS calculation") {
//
//        should("include server tickTimes and all dimensions") {
//            every { server.tickTimes } returns LongArray(100) { 50_000_000 }
//            every { TpsHelper.calculateTps(any()) } returnsMany listOf(18.0, 16.0, 14.0)
//
//            val dim1 = mockk<ResourceKey<Level>>()
//            val dim2 = mockk<ResourceKey<Level>>()
//
//            every { level1.dimension() } returns dim1
//            every { level2.dimension() } returns dim2
//
//            every { TpsHelper.safeCalculate(level1) } returns 16.0
//            every { TpsHelper.safeCalculate(level2) } returns 14.0
//
//            val field = TpsMonitor::class.java.getDeclaredMethod(
//                "calculateGlobalTps",
//                MinecraftServer::class.java
//            )
//            field.isAccessible = true
//
//            val result = field.invoke(TpsMonitor, server) as Double
//
//            result shouldBeExactly (18.0 + 16.0 + 14.0) / 3
//        }
//
//        should("ignore dimensions returning null") {
//            every { server.tickTimes } returns LongArray(100) { 50_000_000 }
//            every { TpsHelper.calculateTps(any()) } returns 20.0
//
//            every { TpsHelper.safeCalculate(any()) } returns null
//
//            val field = TpsMonitor::class.java.getDeclaredMethod(
//                "calculateGlobalTps",
//                MinecraftServer::class.java
//            )
//            field.isAccessible = true
//
//            val result = field.invoke(TpsMonitor, server) as Double
//
//            result shouldBeExactly 20.0
//        }
//    }
})
