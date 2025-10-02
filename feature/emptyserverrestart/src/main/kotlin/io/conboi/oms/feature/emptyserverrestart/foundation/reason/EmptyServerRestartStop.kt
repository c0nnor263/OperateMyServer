package io.conboi.oms.feature.emptyserverrestart.foundation.reason

import io.conboi.oms.common.foundation.reason.OmsStopReason

object EmptyServerRestartStop : OmsStopReason {
    override val name: String = "empty_server_restart"
}