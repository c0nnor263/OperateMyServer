package io.conboi.oms.api.foundation.logging

import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.file.AddonPaths

object OMSLogger {

    fun forFeature(
        feature: FeatureInfo,
        addonPaths: AddonPaths
    ): FeatureLoggerBuilder {
        return FeatureLoggerBuilder(feature, addonPaths)
    }
}
