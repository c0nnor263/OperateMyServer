package io.conboi.oms.api

import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.api.infrastructure.config.FeatureConfig

class TestFeature(provider: ConfigProvider<FeatureConfig>) :
    OmsFeature<FeatureConfig>(provider)