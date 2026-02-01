package io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config

import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.config.FeatureConfigImpl

class CScheduledRestartFeature : FeatureConfigImpl() {
    companion object Companion {
        const val NAME = "scheduled_restart"
    }

    override val name: String = NAME

    val restartTimes = list(
        listOf("00:00", "06:00", "12:00", "18:00"),
        "times",
        Comments.RESTART_TIMES
    ) { element ->
        element?.let { TimeFormatter.parseToLocalTimeOrNull(it) } != null
    }

    val warningTimes = list(
        listOf(
            "2h",
            "30m",
            "15m",
            "10m",
            "5m",
            "2m",
            "1m",
            "30s",
            "15s",
            "10s",
            "5s",
            "4s",
            "3s",
            "2s",
            "1s"
        ),
        "warning_times",
        Comments.WARNING_TIMES
    ) { element ->
        element?.let { TimeFormatter.parseToDurationOrNull(it) } != null
    }

    object Comments {
        const val AUTO_RESTART =
            "This feature allows the server to automatically restart at specified times or when certain conditions are met."
        const val RESTART_TIMES =
            "Times in 24-hour format on which the server will automatically restart\n" +
                    "e.g. [00:00, 06:00, 12:00, 18:00]"

        const val WARNING_TIMES =
            "Times in which the server will send a warning message before restarting\n" +
                    "e.g. [2h, 1h, 10m, 5s]\n" +
                    "The warning time is relative to the restart time, so if the restart time is 00:00 and the warning time is 10s, " +
                    "the server will start sending warning messages at 23:59:50"
    }
}