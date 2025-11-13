package io.conboi.oms

import io.conboi.oms.addon.bundled.scheduledrestart.ScheduledRestartFeature
import io.conboi.oms.api.OmsAddons
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.core.OperateMyServer
import io.conboi.oms.infrastructure.config.OMSConfigs

class OperateMyServerAddon : OmsAddon(OperateMyServer.MOD_ID) {
    override fun onRegisterFeatures(features: List<OmsFeature<*>>) {
        super.onRegisterFeatures(
            listOf(
                ScheduledRestartFeature {
                    OMSConfigs.server.features.scheduledRestart
                }
            )
        )
    }
}

val OmsAddons.oms: OperateMyServerAddon
    get() = get(OperateMyServer.MOD_ID) as OperateMyServerAddon