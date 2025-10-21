package io.conboi.oms.core.foundation.reason

import io.conboi.oms.api.OperateMyServer
import io.conboi.oms.api.foundation.reason.StopReason

interface OmsStopReason : StopReason {
    override val messageId: String
        get() = "${OperateMyServer.MOD_ID}.stop_reason.$name"
}