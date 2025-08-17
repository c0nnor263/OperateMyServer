package io.conboi.restartmyserver.util

import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration

object TimeFormatter {
    fun parseToLocalTimeOrNull(s: String): LocalTime? =
        runCatching { LocalTime.parse(s.trim()) }.getOrNull()

    fun parseToDurationOrNull(s: String): Duration? =
        runCatching { Duration.parse(s.trim()) }.getOrNull()

    fun pickClosestTarget(now: ZonedDateTime, times: List<LocalTime>): ZonedDateTime {
        val candidatesToday = times.map { t ->
            now.withHour(t.hour).withMinute(t.minute).withSecond(0).withNano(0)
        }
        val today = candidatesToday.firstOrNull { it.isAfter(now) }
        if (today != null) return today
        val earliest = times.minBy { it.toSecondOfDay() }
        return now.plusDays(1).withHour(earliest.hour).withMinute(earliest.minute).withSecond(0).withNano(0)
    }

    fun secondsBetween(start: ZonedDateTime, end: ZonedDateTime): Long {
        return ChronoUnit.SECONDS.between(start, end)
    }

    fun formatShort(d: Duration): String {
        val h = d.inWholeHours
        if (h > 0) return "${h}h"
        val m = d.inWholeMinutes
        if (m > 0) return "${m}m"
        return "${d.inWholeSeconds}s"
    }
}