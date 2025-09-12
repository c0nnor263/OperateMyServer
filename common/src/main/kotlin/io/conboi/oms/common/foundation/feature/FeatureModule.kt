package io.conboi.oms.common.foundation.feature

import io.conboi.oms.common.infrastructure.config.FeatureConfigBase

interface FeatureModule<T: FeatureConfigBase> {
    fun register(config: T)
}