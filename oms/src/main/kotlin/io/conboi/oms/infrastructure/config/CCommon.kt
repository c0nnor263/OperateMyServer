package io.conboi.oms.infrastructure.config

import io.conboi.oms.common.infrastructure.config.ConfigBase

class CCommon : ConfigBase() {
    override val name: String = "common"

    val loggingStopReason = b(
        false,
        "loggingStopReason",
        Comments.LOGGING_STOP_REASON
    )

    object Comments {
        const val LOGGING_STOP_REASON =
            "If enabled, the server will log the reason for stopping to a persistent log file."
    }
}