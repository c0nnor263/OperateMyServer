package io.conboi.operatemyserver.infrastructure.config

import io.conboi.operatemyserver.common.foundation.feature.FeatureInfo
import io.conboi.operatemyserver.common.infrastructure.config.ConfigBase
import io.conboi.operatemyserver.common.infrastructure.config.FeatureConfigBase
import io.conboi.operatemyserver.feature.autorestart.infrastructure.config.CAutoRestartFeature
import io.conboi.operatemyserver.feature.lowtps.infrastructure.config.CLowTpsFeature


class CFeatures : ConfigBase() {
    override val name: String = "features"

    val autoRestartFeature: CAutoRestartFeature = nested(
        0,
        { CAutoRestartFeature() },
        Comments.AUTO_RESTART
    )

    val lowTpsFeature: CLowTpsFeature = nested(
        0,
        { CLowTpsFeature() },
        Comments.LOW_TPS
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : FeatureConfigBase> getConfigByFeatureType(type: FeatureInfo.Type): T {
        return when (type) {
            FeatureInfo.Type.AUTO_RESTART -> autoRestartFeature
            FeatureInfo.Type.LOW_TPS -> lowTpsFeature
        } as T
    }

    object Comments {
        const val AUTO_RESTART =
            "This feature allows the server to automatically restart at specified times or when certain conditions are met."
        const val LOW_TPS =
            "This feature monitors the server's TPS (ticks per second) and can trigger a restart if the TPS drops below a defined threshold."
    }
}

