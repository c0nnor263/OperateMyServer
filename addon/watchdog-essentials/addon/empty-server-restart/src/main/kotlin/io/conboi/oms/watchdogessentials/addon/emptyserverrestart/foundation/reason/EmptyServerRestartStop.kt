package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.reason

import io.conboi.oms.watchdogessentials.common.foundation.reason.WEStopReason

object EmptyServerRestartStop : WEStopReason {
    override val name: String = "empty_server_restart"
}