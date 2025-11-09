package io.conboi.oms.utils.foundation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeHelperTest : FunSpec({

    context("secondsBetween") {
        test("returns positive difference") {
            val start = ZonedDateTime.parse("2025-10-31T12:00:00Z")
            val end = ZonedDateTime.parse("2025-10-31T12:00:30Z")
            TimeHelper.secondsBetween(start, end) shouldBe 30
        }

        test("returns negative difference if start is after end") {
            val start = ZonedDateTime.parse("2025-10-31T12:01:00Z")
            val end = ZonedDateTime.parse("2025-10-31T12:00:30Z")
            TimeHelper.secondsBetween(start, end) shouldBe -30
        }
    }

    context("convertLocalTimeToZonedDateTime") {
        test("returns ZonedDateTime with same date but different time") {
            val now = ZonedDateTime.parse("2025-10-31T12:00:00Z")
            val time = LocalTime.of(15, 30)
            TimeHelper.convertLocalTimeToZonedDateTime(now, time) shouldBe
                    ZonedDateTime.parse("2025-10-31T15:30:00Z")
        }

        test("returns ZonedDateTime with second and nano reset") {
            val now = ZonedDateTime.parse("2025-10-31T12:00:45.999999999Z")
            val time = LocalTime.of(8, 0)
            val result = TimeHelper.convertLocalTimeToZonedDateTime(now, time)
            result.second shouldBe 0
            result.nano shouldBe 0
        }
    }

    context("closest") {
        test("returns the first future date") {
            val now = ZonedDateTime.parse("2025-10-31T12:00:00Z")
            val candidates = listOf(
                ZonedDateTime.parse("2025-10-31T13:00:00Z"),
                ZonedDateTime.parse("2025-10-31T14:00:00Z"),
                ZonedDateTime.parse("2025-10-31T15:00:00Z")
            )
            TimeHelper.closest(now, candidates) shouldBe ZonedDateTime.parse("2025-10-31T13:00:00Z")
        }

        test("returns null if all candidates are before now") {
            val now = ZonedDateTime.parse("2025-10-31T16:00:00Z")
            val candidates = listOf(
                ZonedDateTime.parse("2025-10-31T13:00:00Z"),
                ZonedDateTime.parse("2025-10-31T14:00:00Z"),
                ZonedDateTime.parse("2025-10-31T15:00:00Z")
            )
            TimeHelper.closest(now, candidates) shouldBe null
        }

        test("returns null if list is empty") {
            TimeHelper.closest(ZonedDateTime.now(), emptyList()) shouldBe null
        }
    }

    context("zone and currentTime") {
        test("zone returns system default zone") {
            TimeHelper.zone shouldBe ZoneId.systemDefault()
        }

        test("currentTime returns current ZonedDateTime in system zone") {
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val actual = TimeHelper.currentTime

            actual.zone shouldBe now.zone
            actual.toLocalDate() shouldBe now.toLocalDate()
        }
    }
})