package io.conboi.oms.watchdogessentials.addon.lowtps.foundation

import io.kotest.core.spec.style.FunSpec

class TpsMonitorTest : FunSpec({
    // TODO: Currently cannot mock final classes like MinecraftServer and ServerLevel easily
//
//    val mockServer: MinecraftServer = mockk(relaxed = true)
//    val mockLevel1: ServerLevel = mockk(relaxed = true)
//    val mockLevel2: ServerLevel = mockk(relaxed = true)
//
//    val now = ZonedDateTime.parse("2025-01-01T10:00:00Z")
//
//    beforeEach {
//        mockkObject(TpsHelper, TimeHelper)
//
//        every { TimeHelper.currentTime } returns now
//        every { TpsHelper.calculateTps(any()) } returns 18.0
//        every { TpsHelper.safeCalculate(any()) } returns 16.0
//
//
//        every { mockServer.tickTimes } returns LongArray(100) { 50_000_000 }
//        every { mockServer.allLevels } returns listOf(mockLevel1, mockLevel2)
//
//        val field = TpsMonitor::class.java.getDeclaredField("history")
//        field.isAccessible = true
//        (field.get(null) as ArrayDeque<*>).clear()
//    }
//
//    afterEach {
//        unmockkAll()
//    }
//
//    context("update") {
//
//        test("should add snapshot and clean up old ones") {
//            val historyField = TpsMonitor::class.java.getDeclaredField("history")
//            historyField.isAccessible = true
//            val history = historyField.get(null) as ArrayDeque<TpsSnapshot>
//
//            val old = now.minusMinutes(TpsMonitor.MAX_RETENTION_MINUTES + 1)
//            history.addLast(TpsSnapshot(old, 10.0))
//            history.addLast(TpsSnapshot(now, 15.0))
//
//            TpsMonitor.update(mockServer)
//
//            history.size shouldBe 2
//            history.first().time shouldBe now
//        }
//    }
//
//    context("averageTpsOver") {
//
//        test("should return average of recent values") {
//            val field = TpsMonitor::class.java.getDeclaredField("history")
//            field.isAccessible = true
//            val history = field.get(null) as ArrayDeque<TpsSnapshot>
//
//            history.addLast(TpsSnapshot(now.minusMinutes(3), 10.0))
//            history.addLast(TpsSnapshot(now.minusMinutes(2), 20.0))
//            history.addLast(TpsSnapshot(now.minusMinutes(1), 30.0))
//
//            every { TimeHelper.currentTime } returns now
//
//            val avg = TpsMonitor.averageTpsOver(3.minutes)
//            avg shouldBeExactly 20.0
//        }
//
//        test("should return DEFAULT_TPS if no values") {
//            val avg = TpsMonitor.averageTpsOver(3.minutes)
//            avg shouldBeExactly TpsHelper.DEFAULT_TPS
//        }
//
//        test("should return DEFAULT_TPS if duration too short") {
//            val field = TpsMonitor::class.java.getDeclaredField("history")
//            field.isAccessible = true
//            val history = field.get(null) as ArrayDeque<TpsSnapshot>
//            history.addLast(TpsSnapshot(now.minusMinutes(0), 5.0))
//
//            val avg = TpsMonitor.averageTpsOver(0.minutes)
//            avg shouldBeExactly TpsHelper.DEFAULT_TPS
//        }
//    }
})