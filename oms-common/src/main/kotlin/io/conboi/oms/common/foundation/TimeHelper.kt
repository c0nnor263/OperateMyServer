package io.conboi.oms.common.foundation

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

object TimeHelper {
    private const val DAY_SECONDS = 86_400L

    val zoneId: ZoneId = ZoneId.systemDefault()

    val currentTime: Long
        get() = System.currentTimeMillis() / 1000

    @Volatile
    private var cachedOffsetSeconds: Long = zoneId.rules
        .getOffset(Instant.ofEpochSecond(currentTime))
        .totalSeconds.toLong()

    @Volatile
    private var lastOffsetUpdateEpoch: Long = -1L

    fun currentOffsetSeconds(nowEpoch: Long = currentTime): Long {
        if (nowEpoch - lastOffsetUpdateEpoch >= 60L || lastOffsetUpdateEpoch < 0L) {
            cachedOffsetSeconds = zoneId.rules
                .getOffset(Instant.ofEpochSecond(nowEpoch))
                .totalSeconds.toLong()
            lastOffsetUpdateEpoch = nowEpoch
        }
        return cachedOffsetSeconds
    }

    fun secondsBetween(startEpoch: Long, endEpoch: Long): Long =
        endEpoch - startEpoch

    fun secondsOfDay(localEpoch: Long): Long {
        val mod = localEpoch % DAY_SECONDS
        return if (mod >= 0) mod else mod + DAY_SECONDS
    }

    fun localMidnightEpoch(targetEpoch: Long, offset: Long): Long {
        val localEpoch = targetEpoch + offset
        val secOfDay = secondsOfDay(localEpoch)
        return localEpoch - secOfDay
    }

    fun closest(nowEpoch: Long, times: List<LocalTime>): Long {
        val offset = currentOffsetSeconds(nowEpoch)
        val todayMidnight = localMidnightEpoch(nowEpoch, offset)

        val nowLocal = nowEpoch + offset
        val nowSeconds = (nowLocal - todayMidnight).toInt()

        val closestTime = times.minBy { time ->
            val sec = time.toSecondOfDay()
            if (sec >= nowSeconds) sec - nowSeconds
            else 86400 - (nowSeconds - sec)
        }

        val sec = closestTime.toSecondOfDay()
        val real = if (sec >= nowSeconds) todayMidnight + sec else todayMidnight + sec + 86400
        return real - offset
    }

    fun midnightOfNextDay(targetEpoch: Long): Long {
        val offset = currentOffsetSeconds(targetEpoch)
        val midnightLocal = localMidnightEpoch(targetEpoch, offset)
        val tomorrowLocalEpoch = midnightLocal + DAY_SECONDS
        return tomorrowLocalEpoch - offset
    }
}
