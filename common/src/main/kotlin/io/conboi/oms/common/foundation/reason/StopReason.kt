package io.conboi.oms.common.foundation.reason

interface StopReason {
    val name: String
    val messageId: String
        get() = "oms.stopping.$name"
}