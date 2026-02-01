package io.conboi.oms.infrastructure.config

import io.conboi.oms.common.infrastructure.config.ConfigBase


class CServer : ConfigBase() {
    override val name: String = "server"

    val features: CFeatures = nested(
        0,
        { CFeatures() },
        Comments.FEATURES
    )
    val common: CCommon = nested(
        0,
        { CCommon() },
        Comments.COMMON
    )

    object Comments {
        const val COMMON = "Common configuration for the OMS"
        const val FEATURES = "Features configuration for the server.\n" +
                "Each feature has its own configuration and can be enabled or disabled independently"
    }
}

