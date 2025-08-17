package io.conboi.restartmyserver.infrastructure

import io.conboi.restartmyserver.foundation.feature.FeatureType
import io.conboi.restartmyserver.infrastructure.config.ConfigBase
import io.conboi.restartmyserver.infrastructure.config.FeatureConfigBase
import io.conboi.restartmyserver.infrastructure.feature.CAutoRestartFeature


class CServer : ConfigBase() {
    override val name: String = "server"

    val autoRestartFeature: CAutoRestartFeature = nested(
        0,
        { CAutoRestartFeature() }
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : FeatureConfigBase> getConfigByFeatureType(type: FeatureType): T {
        return when (type) {
            FeatureType.AUTO_RESTART -> autoRestartFeature
        } as T
    }
}

