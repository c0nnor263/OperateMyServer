package io.conboi.oms.utils.foundation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeHelperTest : FunSpec({

    context("secondsBetween") {

        test("should return positive difference when end is after start") {
            val start = ZonedDateTime.parse("2025-10-31T12:00:00Z")
            val end = ZonedDateTime.parse("2025-10-31T12:00:30Z")
            TimeHelper.secondsBetween(start, end) shouldBe 30
        }

        test("should return negative difference when start is after end") {
            val start = ZonedDateTime.parse("2025-10-31T12:01:00Z")
            val end = ZonedDateTime.parse("2025-10-31T12:00:30Z")
            TimeHelper.secondsBetween(start, end) shouldBe -30
        }
    }

    context("convertLocalTimeToZonedDateTime") {

        test("should return ZonedDateTime with same date but different time") {
            val now = ZonedDateTime.parse("2025-10-31T12:00:00Z")
            val time = LocalTime.of(15, 30)
            val result = TimeHelper.convertLocalTimeToZonedDateTime(now, time)
            result shouldBe ZonedDateTime.parse("2025-10-31T15:30:00Z")
        }

        test("should reset seconds and nanos to zero") {
            val now = ZonedDateTime.parse("2025-10-31T12:00:45.999999999Z")
            val time = LocalTime.of(8, 0)
            val result = TimeHelper.convertLocalTimeToZonedDateTime(now, time)
            result.second shouldBe 0
            result.nano shouldBe 0
        }
    }

    context("closest") {

        test("should return the first future candidate") {
            val now = ZonedDateTime.parse("2025-10-31T12:00:00Z")
            val candidates = listOf(
                ZonedDateTime.parse("2025-10-31T13:00:00Z"),
                ZonedDateTime.parse("2025-10-31T14:00:00Z"),
                ZonedDateTime.parse("2025-10-31T15:00:00Z")
            )
            val result = TimeHelper.closest(now, candidates)
            result shouldBe ZonedDateTime.parse("2025-10-31T13:00:00Z")
        }

        test("should return null if all candidates are before now") {
            val now = ZonedDateTime.parse("2025-10-31T16:00:00Z")
            val candidates = listOf(
                ZonedDateTime.parse("2025-10-31T13:00:00Z"),
                ZonedDateTime.parse("2025-10-31T14:00:00Z"),
                ZonedDateTime.parse("2025-10-31T15:00:00Z")
            )
            val result = TimeHelper.closest(now, candidates)
            result shouldBe null
        }

        test("should return null if candidate list is empty") {
            val now = ZonedDateTime.now()
            val result = TimeHelper.closest(now, emptyList())
            result shouldBe null
        }
    }

    context("zone") {

        test("should return system default zone") {
            TimeHelper.zone shouldBe ZoneId.systemDefault()
        }
    }

    context("currentTime") {

        test("should return current time in system zone") {
            val expected = ZonedDateTime.now(ZoneId.systemDefault())
            val actual = TimeHelper.currentTime

            actual.zone shouldBe expected.zone
            actual.toLocalDate() shouldBe expected.toLocalDate()
        }
    }
})
