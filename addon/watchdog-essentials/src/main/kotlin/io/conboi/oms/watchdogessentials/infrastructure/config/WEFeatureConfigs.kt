package io.conboi.oms.watchdogessentials.infrastructure.config

import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.lowtps.infrastructure.config.CLowTpsFeature
import io.conboi.oms.watchdogessentials.core.infrastructure.config.CFeatures

object WEFeatureConfigs {
    fun register() {
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