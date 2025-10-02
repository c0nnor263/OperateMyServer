package io.conboi.oms.feature.lowtps.foundation.reason

import io.conboi.oms.common.foundation.reason.OmsStopReason

object LowTpsStop : OmsStopReason {
    override val name: String = "low_tps"
}