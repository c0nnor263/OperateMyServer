package io.conboi.oms.watchdogessentials

import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.lowtps.LowTpsFeature
import io.conboi.oms.watchdogessentials.core.WatchDogEssentials
import io.conboi.oms.watchdogessentials.infrastructure.config.WEConfigs

class WatchDogEssentialsAddon : OmsAddon(WatchDogEssentials.MOD_ID) {
    override fun onRegisterFeatures(features: List<OmsFeature<*>>) {
        val featureConfig = WEConfigs.server.features
        super.onRegisterFeatures(
            listOf(
                LowTpsFeature {
                    featureConfig.lowTps
                },
                EmptyServerRestartFeature {
                    featureConfig.emptyServerRestart
                }
            )
        )
    }
}