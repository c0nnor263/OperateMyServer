package io.conboi.oms.watchdogessentials.core.foundation.reason

import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.watchdogessentials.core.WatchDogEssentials

interface WEStopReason : StopReason {
    override val addonId: String
        get() = WatchDogEssentials.MOD_ID
}