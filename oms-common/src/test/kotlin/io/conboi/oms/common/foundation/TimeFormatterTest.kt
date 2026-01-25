package io.conboi.oms.common.foundation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimeFormatterTest : ShouldSpec({

    context("parseToLocalTimeOrNull") {

        should("parse valid time with spaces") {
            TimeFormatter.parseToLocalTimeOrNull("  12:34  ") shouldBe LocalTime.of(12, 34)
        }

        should("return null for empty or blank input") {
            TimeFormatter.parseToLocalTimeOrNull("") shouldBe null
            TimeFormatter.parseToLocalTimeOrNull("   ") shouldBe null
        }
    }

    context("parseToDurationOrNull") {

        should("parse ISO-8601 duration with spaces") {
            TimeFormatter.parseToDurationOrNull("  PT1H  ") shouldBe 1.hours
        }

        should("return null for empty or blank input") {
            TimeFormatter.parseToDurationOrNull("") shouldBe null
            TimeFormatter.parseToDurationOrNull("   ") shouldBe null
        }
    }

    context("formatDuration") {

        should("format zero duration as seconds") {
            TimeFormatter.formatDuration(0.seconds) shouldBe "0s"
        }

        should("format negative durations as seconds") {
            TimeFormatter.formatDuration((-5).seconds) shouldBe "-5s"
        }

        should("format hours and ignore minutes") {
            TimeFormatter.formatDuration(1.hours + 30.minutes) shouldBe "1h"
        }

        should("format minutes and ignore seconds") {
            TimeFormatter.formatDuration(45.minutes + 30.seconds) shouldBe "45m"
        }
    }

    context("formatDateTime") {

        val zone = ZoneId.of("UTC")
        val nowZdt = ZonedDateTime.of(
            2025, 10, 31,
            10, 0, 0, 0,
            zone
        )

        should("use HH:mm formatter for today's date") {
            val target = nowZdt.withHour(12).withMinute(30)
            val formatted = TimeFormatter.formatDateTime(target.toEpochSecond(), nowZdt.toEpochSecond(), zone)
            formatted shouldBe "12:30"
        }

        should("use dd.MM HH:mm formatter for a different date") {
            val target = ZonedDateTime.of(
                2025, 11, 1,
                8, 15, 0, 0,
                zone
            )
            val formatted = TimeFormatter.formatDateTime(target.toEpochSecond(), nowZdt.toEpochSecond(), zone)
            formatted shouldBe "01.11 08:15"
        }

        should("format leap day correctly") {
            val target = ZonedDateTime.of(
                2024, 2, 29,
                12, 34, 56, 0,
                zone
            )
            val formatted = TimeFormatter.formatDateTime(target.toEpochSecond(), nowZdt.toEpochSecond(), zone)
            formatted shouldBe "29.02 12:34"
        }
    }
})
