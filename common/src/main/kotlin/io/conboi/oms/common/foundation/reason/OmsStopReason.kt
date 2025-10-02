package io.conboi.oms.common.foundation.reason

import io.conboi.oms.api.foundation.reason.StopReason

interface OmsStopReason : StopReason {
    override val messageId: String
        get() = "oms.stop_reason.$name"
}