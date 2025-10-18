package io.conboi.oms.addon.bundled.emptyserverrestart.foundation.reason

import io.conboi.oms.core.foundation.reason.OmsStopReason

object EmptyServerRestartStop : OmsStopReason {
    override val name: String = "empty_server_restart"
}