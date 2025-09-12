package io.conboi.oms.feature.lowtps

import io.conboi.oms.common.OMSFeatureManager
import io.conboi.oms.common.foundation.feature.FeatureInfo
import io.conboi.oms.common.foundation.feature.FeatureModule
import io.conboi.oms.feature.lowtps.infrastructure.config.CLowTpsFeature

object LowTpsFeatureModule : FeatureModule<CLowTpsFeature> {
    override fun register(config: CLowTpsFeature) {
        val featureInfo = FeatureInfo(
            type = FeatureInfo.Type.LOW_TPS,
            priority = FeatureInfo.Priority.CRITICAL
        )
        val feature = LowTpsFeature(config, featureInfo)
        OMSFeatureManager.register(feature)
    }
}