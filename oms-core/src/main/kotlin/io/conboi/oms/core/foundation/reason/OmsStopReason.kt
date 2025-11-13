package io.conboi.oms.core.foundation.reason

import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.core.OperateMyServer

interface OmsStopReason : StopReason {
    override val messageId: String
        get() = "${OperateMyServer.MOD_ID}.stop_reason.$name"
}