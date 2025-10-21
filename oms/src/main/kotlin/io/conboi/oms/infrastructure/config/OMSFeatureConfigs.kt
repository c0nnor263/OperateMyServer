package io.conboi.oms.infrastructure.config

import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CAutoRestartFeature
import io.conboi.oms.core.infrastructure.config.CFeatures

object OMSFeatureConfigs {
    fun register() {
        CFeatures.registerFeatureConfig {
            nested(
                0,
                { CAutoRestartFeature() },
                CAutoRestartFeature.Comments.AUTO_RESTART
            )
        }
    }
}