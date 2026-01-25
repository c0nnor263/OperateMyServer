package io.conboi.oms

import io.conboi.oms.addon.bundled.scheduledrestart.ScheduledRestartFeature
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.infrastructure.config.OMSConfigs

internal class OperateMyServerAddon : OmsAddon(OperateMyServer.MOD_ID) {

    override fun onRegisterFeatures(context: AddonContext) {
        super.onRegisterFeatures(context)
        context.featureManager.register(
            ScheduledRestartFeature {
                OMSConfigs.server.features.scheduledRestart
            }
        )
    }
}