package io.conboi.oms.utils.foundation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimeFormatterTest : FunSpec({

    context("parseToLocalTimeOrNull") {
        test("should parse valid time with spaces") {
            TimeFormatter.parseToLocalTimeOrNull("  12:34  ") shouldBe LocalTime.of(12, 34)
        }

        test("should return null for empty string") {
            TimeFormatter.parseToLocalTimeOrNull("") shouldBe null
        }

        test("should return null for blank string") {
            TimeFormatter.parseToLocalTimeOrNull("   ") shouldBe null
        }
    }

    context("parseToDurationOrNull") {
        test("should parse ISO-8601 duration with spaces") {
            TimeFormatter.parseToDurationOrNull("  PT1H  ") shouldBe 1.hours
        }

        test("should return null for empty string") {
            TimeFormatter.parseToDurationOrNull("") shouldBe null
        }

        test("should return null for blank string") {
            TimeFormatter.parseToDurationOrNull("   ") shouldBe null
        }
    }

    context("formatDuration") {
        test("should format zero duration as seconds") {
            TimeFormatter.formatDuration(0.seconds) shouldBe "0s"
        }

        test("should format negative duration as seconds") {
            TimeFormatter.formatDuration((-5).seconds) shouldBe "-5s"
        }

        test("should format hours and ignore minutes/seconds") {
            TimeFormatter.formatDuration(1.hours + 30.minutes) shouldBe "1h"
        }

        test("should format minutes and ignore seconds if no hours") {
            TimeFormatter.formatDuration(45.minutes + 30.seconds) shouldBe "45m"
        }
    }

    context("formatZonedDateTime with default formatter") {
        test("should format in HH:mm regardless of zone offset") {
            val time = ZonedDateTime.parse("2025-10-31T12:34:56+02:00")
            TimeFormatter.formatZonedDateTime(time) shouldBe "12:34"
        }

        test("should format leap day correctly") {
            val time = ZonedDateTime.parse("2024-02-29T12:34:56Z")
            TimeFormatter.formatZonedDateTime(time) shouldBe "12:34"
        }

        test("should format non-leap year correctly") {
            val time = ZonedDateTime.parse("2023-02-28T12:34:56Z")
            TimeFormatter.formatZonedDateTime(time) shouldBe "12:34"
        }
    }

    context("formatZonedDateTime with ddMMHHmmFormatter") {
        test("should format correctly on October 31") {
            val time = ZonedDateTime.parse("2025-10-31T12:34:56Z")
            TimeFormatter.formatZonedDateTime(time, TimeFormatter.ddMMHHmmFormatter) shouldBe "31.10 12:34"
        }

        test("should format correctly on December 1") {
            val time = ZonedDateTime.parse("2025-12-01T08:15:00Z")
            TimeFormatter.formatZonedDateTime(time, TimeFormatter.ddMMHHmmFormatter) shouldBe "01.12 08:15"
        }
    }
})