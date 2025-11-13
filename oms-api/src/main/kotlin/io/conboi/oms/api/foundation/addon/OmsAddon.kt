package io.conboi.oms.api.foundation.addon

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.file.AddonPaths
import io.conboi.oms.api.foundation.info.InfoProvider
import io.conboi.oms.api.foundation.logging.OMSLogger
import io.conboi.oms.api.foundation.manager.FeatureManager
import java.nio.file.Path

abstract class OmsAddon(val id: String) : InfoProvider<OmsAddonInfo> {
    open val featureManager: FeatureManager = object : FeatureManager(id) {}
    open val paths: AddonPaths = object : AddonPaths(id) {}
    open val logger: OMSLogger = OMSLogger

    init {
        check(isValidId(id)) { "Invalid addon id: $id. It must only contain lowercase letters, numbers, underscores, and hyphens." }
    }

    override fun info(): OmsAddonInfo {
        return OmsAddonInfo(
            id = id,
            featureManagerInfo = featureManager.info()
        )
    }


    open fun onInitializeOmsRoot(omsRootPath: Path) {
        paths.onInitializeOmsRoot(omsRootPath)
    }

    open fun onRegisterFeatures(features: List<OmsFeature<*>> = emptyList()) {
        features.forEach { feature -> featureManager.register(feature) }
    }

    open fun onRegisterConfigs() {
        featureManager.onRegisterConfig()
    }

    open fun onFreeze() {
        featureManager.freeze()
    }

    open fun onOmsStarted(event: OMSLifecycle.StartingEvent) {
        featureManager.onStartingEvent(event)
    }

    open fun onOmsTick(event: OMSLifecycle.TickingEvent) {
        featureManager.onTickingEvent(event)
    }

    open fun onOmsStopping(event: OMSLifecycle.StoppingEvent) {
        featureManager.onStoppingEvent(event)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : OmsFeature<*>> getFeatureById(id: String): T? {
        return featureManager.getFeatureById(id)
    }

    private fun isValidId(id: String): Boolean {
        return id.matches(Regex("[a-z0-9_\\-]+"))
    }
}