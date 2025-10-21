package io.conboi.oms.watchdogessentials.core.foundation.reason

import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.watchdogessentials.core.WatchDogEssentials

interface WEStopReason : StopReason {
    override val messageId: String
        get() = "${WatchDogEssentials.MOD_ID}.stop_reason.$name"
}