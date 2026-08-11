package io.conboi.oms.infrastructure.config

import io.conboi.oms.common.infrastructure.config.ConfigBase

class CCommon : ConfigBase() {
    override val name: String = "common"

    val logStopReasonToFile = b(
        false,
        "log_stop_reason_to_file",
        Comments.LOG_STOP_REASON_TO_FILE
    )

    object Comments {
        const val LOG_STOP_REASON_TO_FILE =
            "If enabled, the server will log the reason for stopping to a persistent log file."
    }
}