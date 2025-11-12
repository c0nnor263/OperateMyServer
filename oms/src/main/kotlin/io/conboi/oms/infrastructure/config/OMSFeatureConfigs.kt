package io.conboi.oms.infrastructure.config

import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.core.infrastructure.config.CFeatures

object OMSFeatureConfigs {
    fun register() {
        CFeatures.registerFeatureConfig {
            nested(
                0,
                { CScheduledRestartFeature() },
                CScheduledRestartFeature.Comments.AUTO_RESTART
            )
        }
    }
}