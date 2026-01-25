package io.conboi.oms.common.foundation.reason

import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.common.OperateMyServer

interface OmsStopReason : StopReason {
    override val addonId: String
        get() = OperateMyServer.MOD_ID
}