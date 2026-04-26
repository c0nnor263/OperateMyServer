package io.conboi.oms.watchdogessentials.feature.emptyserver.foundation.reason

import io.conboi.oms.watchdogessentials.common.foundation.reason.WEStopReason

object EmptyServerStop : WEStopReason {
    override val name: String = "empty_server"
}