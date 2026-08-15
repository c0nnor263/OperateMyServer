package io.conboi.oms.watchdogessentials.feature.lowtps.infrastructure.config

import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.config.FeatureConfigImpl
import io.conboi.oms.watchdogessentials.feature.lowtps.foundation.TpsSnapshotHistory

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

    val tpsAveragingWindow = s(
        "2m",
        "tps_averaging_window",
        Comments.TPS_AVERAGING_WINDOW
    ) { value ->
        val tpsAveragingWindow = value?.let {
            TimeFormatter.parseToDurationOrNull(it)
        } ?: return@s false
        tpsAveragingWindow in TpsSnapshotHistory.MIN_RETENTION_MINUTES..TpsSnapshotHistory.MAX_RETENTION_MINUTES
    }

    object Comments {
        const val LOW_TPS =
            "This feature monitors the server's TPS (ticks per second) and can trigger a restart if the TPS drops below a defined threshold."
        const val TPS_THRESHOLD =
            "The TPS threshold below which the server is considered to be under low TPS conditions"
        const val TPS_AVERAGING_WINDOW =
            "The time window over which the TPS is averaged to determine if it is below the threshold"
    }
}