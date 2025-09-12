package io.conboi.oms.feature.autorestart

import io.conboi.oms.common.OMSFeatureManager
import io.conboi.oms.common.foundation.feature.FeatureInfo
import io.conboi.oms.common.foundation.feature.FeatureModule
import io.conboi.oms.feature.autorestart.infrastructure.config.CAutoRestartFeature

object AutoRestartFeatureModule : FeatureModule<CAutoRestartFeature> {
    override fun register(config: CAutoRestartFeature) {
        val featureInfo = FeatureInfo(
            type = FeatureInfo.Type.AUTO_RESTART,
            priority = FeatureInfo.Priority.COMMON
        )
        val feature = AutoRestartFeature(config, featureInfo)
        OMSFeatureManager.register(feature)
    }
}