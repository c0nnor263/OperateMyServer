package io.conboi.oms.watchdogessentials.addon.lowtps.foundation

import java.time.ZonedDateTime

internal data class TpsSnapshot(
    val time: ZonedDateTime,
    val value: Double
)