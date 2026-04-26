package io.conboi.oms.watchdogessentials

import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.watchdogessentials.common.WatchDogEssentials
import io.conboi.oms.watchdogessentials.feature.emptyserver.EmptyServerFeature
import io.conboi.oms.watchdogessentials.feature.lowtps.LowTpsFeature
import io.conboi.oms.watchdogessentials.infrastructure.config.WEConfigs

class WatchDogEssentialsAddon : OmsAddon(WatchDogEssentials.MOD_ID) {

    override fun onRegisterFeatures(context: AddonContext) {
        super.onRegisterFeatures(context)
        val featureConfig = WEConfigs.server.features
        listOf(
            LowTpsFeature {
                featureConfig.lowTps
            },
            EmptyServerFeature {
                featureConfig.emptyServer
            }
        ).forEach { feature ->
            context.featureManager.register(feature)
        }
    }
}