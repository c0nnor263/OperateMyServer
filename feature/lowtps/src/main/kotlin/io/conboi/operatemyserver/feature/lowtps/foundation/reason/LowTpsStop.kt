package io.conboi.operatemyserver.feature.lowtps.foundation.reason

import io.conboi.operatemyserver.common.foundation.reason.StopReason

object LowTpsStop : StopReason {
    override val name: String = "low_tps"
}