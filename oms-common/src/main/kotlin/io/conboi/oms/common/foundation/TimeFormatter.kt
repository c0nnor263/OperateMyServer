package io.conboi.oms.common.foundation

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
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

    fun formatDateTime(
        epochSeconds: Long,
        nowEpoch: Long = TimeHelper.currentTime,
        zoneId: ZoneId = TimeHelper.zoneId
    ): String {
        val target = Instant.ofEpochSecond(epochSeconds).atZone(zoneId)
        val now = Instant.ofEpochSecond(nowEpoch).atZone(zoneId)

        val formatter = if (target.toLocalDate().isEqual(now.toLocalDate())) {
            HHmmFormatter
        } else {
            ddMMHHmmFormatter
        }

        return target.format(formatter)
    }
}
