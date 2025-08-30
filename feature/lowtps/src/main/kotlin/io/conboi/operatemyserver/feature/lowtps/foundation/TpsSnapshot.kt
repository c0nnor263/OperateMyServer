package io.conboi.operatemyserver.feature.lowtps.foundation

import java.time.ZonedDateTime

data class TpsSnapshot(val time: ZonedDateTime, val value: Double)