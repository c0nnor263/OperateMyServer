package io.conboi.oms.watchdogessentials.infrastructure.config

import io.conboi.oms.common.infrastructure.config.ConfigBase
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.lowtps.infrastructure.config.CLowTpsFeature

class CFeatures : ConfigBase() {
    override val name: String = "features"

    val lowTps =
        nested(
            0,
            { CLowTpsFeature() },
            CLowTpsFeature.Comments.LOW_TPS
        )

    val emptyServerRestart =
        nested(
            0,
            { CEmptyServerRestartFeature() },
            CEmptyServerRestartFeature.Comments.EMPTY_SERVER_RESTART
        )

}

