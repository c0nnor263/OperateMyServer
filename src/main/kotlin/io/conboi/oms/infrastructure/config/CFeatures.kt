package io.conboi.oms.infrastructure.config

import io.conboi.oms.common.infrastructure.config.ConfigBase
import io.conboi.oms.feature.autorestart.infrastructure.config.CAutoRestartFeature
import io.conboi.oms.feature.lowtps.infrastructure.config.CLowTpsFeature

class CFeatures : ConfigBase() {
    override val name: String = "features"

    val autoRestart: CAutoRestartFeature = nested(
        0,
        { CAutoRestartFeature() },
        Comments.AUTO_RESTART
    )

    val lowTps: CLowTpsFeature = nested(
        0,
        { CLowTpsFeature() },
        Comments.LOW_TPS
    )

    object Comments {
        const val AUTO_RESTART =
            "This feature allows the server to automatically restart at specified times or when certain conditions are met."
        const val LOW_TPS =
            "This feature monitors the server's TPS (ticks per second) and can trigger a restart if the TPS drops below a defined threshold."
    }
}

