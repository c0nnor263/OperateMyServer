package io.conboi.oms.watchdogessentials.infrastructure.config

import io.conboi.oms.common.infrastructure.config.ConfigBase
import io.conboi.oms.watchdogessentials.feature.emptyserver.infrastructure.config.CEmptyServerFeature
import io.conboi.oms.watchdogessentials.feature.lowtps.infrastructure.config.CLowTpsFeature

class CFeatures : ConfigBase() {
    override val name: String = "features"

    val lowTps =
        nested(
            0,
            { CLowTpsFeature() },
            CLowTpsFeature.Comments.LOW_TPS
        )

    val emptyServer =
        nested(
            0,
            { CEmptyServerFeature() },
            CEmptyServerFeature.Comments.EMPTY_SERVER
        )

}

