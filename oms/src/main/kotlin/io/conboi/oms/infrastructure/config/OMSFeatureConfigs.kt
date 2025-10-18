package io.conboi.oms.infrastructure.config

import io.conboi.oms.addon.bundled.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.addon.bundled.lowtps.infrastructure.config.CLowTpsFeature
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
        CFeatures.registerFeatureConfig {
            nested(
                0,
                { CLowTpsFeature() },
                CLowTpsFeature.Comments.LOW_TPS
            )
        }
        CFeatures.registerFeatureConfig {
            nested(
                0,
                { CEmptyServerRestartFeature() },
                CEmptyServerRestartFeature.Comments.EMPTY_SERVER_RESTART
            )
        }
    }
}