package io.conboi.oms.watchdogessentials.common.foundation.reason

import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.watchdogessentials.common.WatchDogEssentials

interface WEStopReason : StopReason {
    override val addonId: String
        get() = WatchDogEssentials.MOD_ID
}