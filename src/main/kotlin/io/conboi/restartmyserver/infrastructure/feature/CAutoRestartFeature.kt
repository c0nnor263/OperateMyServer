package io.conboi.restartmyserver.infrastructure.feature

import io.conboi.restartmyserver.infrastructure.config.FeatureConfigBase

class CAutoRestartFeature : FeatureConfigBase() {
    override val featureName: String = "auto_restart"
    val restartTimes = list(
        mutableListOf("00:00"),
        "times",
        Comments.AUTO_RESTART_TIMES
    )

    val warningTimes = list(
        mutableListOf("2h", "30m", "10m", "5m", "2m", "1m", "30s", "5s", "4s", "3s", "2s", "1s"),
        "warning_times",
        Comments.AUTO_RESTART_WARNING_TIMES
    )

    object Comments {
        const val AUTO_RESTART_TIMES =
            "Times in 24-hour format on which the server will automatically restart\n" +
                    "e.g. [00:00, 06:00, 12:00, 18:00]"

        const val AUTO_RESTART_WARNING_TIMES =
            "Times in which the server will send a warning message before restarting\n" +
                    "e.g. [2h, 1h, 10m, 5s]\n" +
                    "The warning time is relative to the restart time, so if the restart time is 00:00 and the warning time is 10s, " +
                    "the server will start sending warning messages at 23:59:50"
    }
}