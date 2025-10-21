package io.conboi.oms.utils.foundation

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object TimeHelper {
    val zone: ZoneId get() = ZoneId.systemDefault()
    val currentTime: ZonedDateTime get() = ZonedDateTime.now(zone)

    fun secondsBetween(start: ZonedDateTime, end: ZonedDateTime): Long {
        return ChronoUnit.SECONDS.between(start, end)
    }

    fun convertLocalTimeToZonedDateTime(now: ZonedDateTime, time: LocalTime): ZonedDateTime {
        return now.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
    }

    fun closest(now: ZonedDateTime, candidates: List<ZonedDateTime>): ZonedDateTime? {
        return candidates.firstOrNull { it.isAfter(now) }
    }
}