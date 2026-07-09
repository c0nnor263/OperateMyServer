package io.conboi.oms.common.foundation

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

object TimeFormatter {
    val HHmmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val ddMMHHmmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM HH:mm")
    val yyyyMMddHHmmssFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    fun parseToLocalTimeOrNull(value: String): LocalTime? =
        runCatching { LocalTime.parse(value.trim()) }.getOrNull()

    fun parseToDurationOrNull(value: String): Duration? =
        runCatching { Duration.parse(value.trim()) }.getOrNull()

    fun formatDuration(duration: Duration): String {
        if (duration.inWholeSeconds == 0L) return "0s"
        if (duration.isNegative()) {
            return "-${formatDuration(-duration)}"
        }

        val hours = duration.inWholeHours
        val minutes = duration.inWholeMinutes % 60
        val seconds = duration.inWholeSeconds % 60

        return buildString {
            if (hours > 0) append("${hours}h")
            if (minutes > 0) append("${minutes}m")
            if (seconds > 0) append("${seconds}s")
        }
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

    fun formatDateTimeFileName(
        epochSeconds: Long,
        zoneId: ZoneId = TimeHelper.zoneId
    ): String {
        return Instant.ofEpochSecond(epochSeconds)
            .atZone(zoneId)
            .format(yyyyMMddHHmmssFormatter)
    }
}
