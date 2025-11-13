package io.conboi.oms.watchdogessentials.infrastructure.config

import io.conboi.oms.utils.infrastructure.config.ConfigBase


class CServer : ConfigBase() {
    override val name: String = "serverrrrrrrrrrr"

    val features: CFeatures = nested(
        0,
        { CFeatures() },
        Comments.FEATURES
    )

    object Comments {
        const val FEATURES = "Features configuration for the server.\n" +
                "Each feature has its own configuration and can be enabled or disabled independently"
    }
}

