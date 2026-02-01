package io.conboi.oms.watchdogessentials.addon.lowtps.infrastructure.config

import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.config.FeatureConfigImpl
import io.conboi.oms.watchdogessentials.addon.lowtps.foundation.TpsMonitor

class CLowTpsFeature : FeatureConfigImpl() {
    companion object {
        const val NAME = "low_tps"
    }

    override val name: String = NAME

    val tpsThreshold = i(
        15,
        min = 5,
        max = 15,
        "tps_threshold",
        Comments.TPS_THRESHOLD,
    )

    val tpsCountTime = s(
        "2m",
        "tps_count_time",
        Comments.TPS_COUNT_TIME
    ) { value ->
        val tpsCountTime = value?.let {
            TimeFormatter.parseToDurationOrNull(it)
        }
        val minutes = tpsCountTime?.inWholeMinutes ?: return@s false
        minutes in TpsMonitor.MIN_RETENTION_MINUTES..TpsMonitor.MAX_RETENTION_MINUTES
    }

    object Comments {
        const val LOW_TPS =
            "This feature monitors the server's TPS (ticks per second) and can trigger a restart if the TPS drops below a defined threshold."
        const val TPS_THRESHOLD =
            "The TPS threshold below which the server is considered to be under low TPS conditions"
        const val TPS_COUNT_TIME =
            "The time over for restarting the server when the TPS is below the threshold. Default is 2 minutes(\"2m\"). Minimum is 1 minute(\"1m\")"
    }
}