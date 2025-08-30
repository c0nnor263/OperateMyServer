package io.conboi.operatemyserver.feature.lowtps.infrastructure.config

import io.conboi.operatemyserver.common.foundation.TimeFormatter
import io.conboi.operatemyserver.common.infrastructure.config.FeatureConfigBase
import io.conboi.operatemyserver.feature.lowtps.foundation.TpsMonitor

class CLowTpsFeature : FeatureConfigBase() {
    override val name: String = "low_tps"

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
        } ?: return@s false
        val minutes = tpsCountTime.inWholeMinutes
        minutes in TpsMonitor.MIN_RETENTION_MINUTES..TpsMonitor.MAX_RETENTION_MINUTES
    }

    object Comments {
        const val TPS_THRESHOLD =
            "The TPS threshold below which the server is considered to be under low TPS conditions"
        const val TPS_COUNT_TIME =
            "The time over for restarting the server when the TPS is below the threshold. Default is 2 minutes(\"2m\"). Minimum is 1 minute(\"1m\")"
    }
}