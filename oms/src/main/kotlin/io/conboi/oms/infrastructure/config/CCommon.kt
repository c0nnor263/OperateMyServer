package io.conboi.oms.infrastructure.config

import io.conboi.oms.common.infrastructure.config.ConfigBase

class CCommon : ConfigBase() {
    override val name: String = "common"

    val stopReasonLogging = b(
        false,
        "stopReasonLogging",
        Comments.STOP_REASON_LOGGING
    )

    object Comments {
        const val STOP_REASON_LOGGING =
            "If enabled, the server will log the reason for stopping to a persistent log file."
    }
}