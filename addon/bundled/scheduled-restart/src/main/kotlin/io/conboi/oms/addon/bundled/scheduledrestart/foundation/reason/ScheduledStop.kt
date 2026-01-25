package io.conboi.oms.addon.bundled.scheduledrestart.foundation.reason

import io.conboi.oms.common.foundation.reason.OmsStopReason

object ScheduledStop : OmsStopReason {
    override val name: String = "scheduled"
}