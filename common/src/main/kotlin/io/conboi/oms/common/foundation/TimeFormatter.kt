package io.conboi.oms.common.foundation

import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

object TimeFormatter {
    val HHmmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val ddMMHHmmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM HH:mm")

    fun parseToLocalTimeOrNull(value: String): LocalTime? =
        runCatching { LocalTime.parse(value.trim()) }.getOrNull()

    fun parseToDurationOrNull(value: String): Duration? =
        runCatching { Duration.parse(value.trim()) }.getOrNull()

    fun formatDuration(duration: Duration): String {
        val h = duration.inWholeHours
        if (h > 0) return "${h}h"
        val m = duration.inWholeMinutes
        if (m > 0) return "${m}m"
        return "${duration.inWholeSeconds}s"
    }

    fun formatZonedDateTime(
        time: ZonedDateTime,
        formatter: DateTimeFormatter = HHmmFormatter
    ): String = time.format(formatter)
}