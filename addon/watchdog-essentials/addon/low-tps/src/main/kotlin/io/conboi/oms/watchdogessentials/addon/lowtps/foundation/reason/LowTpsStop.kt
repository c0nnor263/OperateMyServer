package io.conboi.oms.watchdogessentials.addon.lowtps.foundation.reason

import io.conboi.oms.watchdogessentials.core.foundation.reason.WEStopReason

object LowTpsStop : WEStopReason {
    override val name: String = "low_tps"
}