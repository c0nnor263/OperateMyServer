package io.conboi.oms.api.foundation.logging

import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.file.AddonPathType
import io.conboi.oms.api.foundation.file.AddonPaths
import kotlin.io.path.name

class FeatureLoggerBuilder(
    private val feature: FeatureInfo,
    private val paths: AddonPaths
) {
    fun persistent(): OMSPersistentFileLogger {
        val logDir = paths.forFeature(feature, AddonPathType.LOGS)
        return OMSPersistentLoggerService.createPersistentLogger(
            // TODO: Not OK
            paths.addonRoot.name,
            feature.id,
            logDir
        )
    }
}
