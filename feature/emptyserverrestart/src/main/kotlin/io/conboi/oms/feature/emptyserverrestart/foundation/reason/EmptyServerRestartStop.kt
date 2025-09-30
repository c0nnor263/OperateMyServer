package io.conboi.oms.feature.emptyserverrestart.foundation.reason

import io.conboi.oms.common.foundation.reason.StopReason

object EmptyServerRestartStop : StopReason {
    override val name: String = "empty_server_restart"
}