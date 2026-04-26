package io.conboi.oms.infrastructure.config

import io.conboi.oms.common.infrastructure.config.ConfigBase
import io.conboi.oms.feature.scheduledrestart.infrastructure.config.CScheduledRestartFeature

class CFeatures : ConfigBase() {
    override val name: String = "features"

    val scheduledRestart = nested(
        0,
        { CScheduledRestartFeature() },
        CScheduledRestartFeature.Comments.AUTO_RESTART
    )
}

