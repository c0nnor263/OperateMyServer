package io.conboi.oms.addon.bundled.lowtps.foundation

import java.time.ZonedDateTime

internal data class TpsSnapshot(
    val time: ZonedDateTime,
    val value: Double
)