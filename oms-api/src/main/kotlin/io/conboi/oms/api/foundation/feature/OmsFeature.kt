package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.infrastructure.config.FeatureConfig


abstract class OmsFeature<out T : FeatureConfig>(
    //TODO Move to init parameter
    val featureInfo: FeatureInfo
) {
    private var _config: T? = null
    val config: T
        get() = _config ?: throw IllegalStateException("Feature config is not initialized yet.")

    fun isEnabled(): Boolean = config.isEnabled()

    fun enable() = config.enable()

    fun disable() = config.disable()

    open fun getFeatureCommands(): List<OMSCommandEntry> = emptyList()

    open fun onOmsTick(event: OMSLifecycle.TickingEvent) {}
    open fun onOmsStarted(event: OMSLifecycle.StartingEvent) {}
    open fun onOmsStopping(event: OMSLifecycle.StoppingEvent) {}

    open fun onOmsRegisterConfig(config: FeatureConfig) {
        @Suppress("UNCHECKED_CAST")
        this._config = config as T
    }
}