package io.conboi.oms.feature.lowtps.foundation

import java.time.ZonedDateTime

internal data class TpsSnapshot(
    val time: ZonedDateTime,
    val value: Double
)