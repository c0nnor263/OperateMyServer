package io.conboi.operatemyserver.common.foundation

import java.time.ZoneId
import java.time.ZonedDateTime

object TimeHelper {
    val zone: ZoneId get() = ZoneId.systemDefault()
    val currentTime: ZonedDateTime get() = ZonedDateTime.now(zone)
}