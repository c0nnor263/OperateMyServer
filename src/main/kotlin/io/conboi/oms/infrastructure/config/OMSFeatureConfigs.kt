package io.conboi.oms.infrastructure.config

import io.conboi.oms.common.infrastructure.config.CFeatures
import io.conboi.oms.feature.autorestart.infrastructure.config.CAutoRestartFeature
import io.conboi.oms.feature.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.feature.lowtps.infrastructure.config.CLowTpsFeature

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