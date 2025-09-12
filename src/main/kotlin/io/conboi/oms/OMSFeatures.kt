package io.conboi.oms

import io.conboi.oms.feature.autorestart.AutoRestartFeatureModule
import io.conboi.oms.feature.lowtps.LowTpsFeatureModule
import io.conboi.oms.infrastructure.config.CServer

object OMSFeatures {
    fun registerAll(config: CServer) {
        AutoRestartFeatureModule.register(config.features.autoRestart)
        LowTpsFeatureModule.register(config.features.lowTps)
    }
}