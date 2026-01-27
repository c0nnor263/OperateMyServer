package io.conboi.oms.watchdogessentials

import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.lowtps.LowTpsFeature
import io.conboi.oms.watchdogessentials.common.WatchDogEssentials
import io.conboi.oms.watchdogessentials.infrastructure.config.WEConfigs

class WatchDogEssentialsAddon : OmsAddon(WatchDogEssentials.MOD_ID) {

    override fun onRegisterFeatures(context: AddonContext) {
        super.onRegisterFeatures(context)
        val featureConfig = WEConfigs.server.features
        listOf(
            LowTpsFeature {
                featureConfig.lowTps
            },
            EmptyServerRestartFeature {
                featureConfig.emptyServerRestart
            }
        ).forEach { feature ->
            context.featureManager.register(feature)
        }
    }
}