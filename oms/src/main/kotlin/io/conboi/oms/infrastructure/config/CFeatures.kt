package io.conboi.oms.infrastructure.config

import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.common.infrastructure.config.ConfigBase

class CFeatures : ConfigBase() {
    override val name: String = "features"

    val scheduledRestart = nested(
        0,
        { CScheduledRestartFeature() },
        CScheduledRestartFeature.Comments.AUTO_RESTART
    )
}

