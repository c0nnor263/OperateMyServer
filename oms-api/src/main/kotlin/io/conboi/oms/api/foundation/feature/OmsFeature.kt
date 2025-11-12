package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.event.OMSFeatureLifecycle
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.infrastructure.config.FeatureConfig


abstract class OmsFeature<out T : FeatureConfig> : ConfigWatcher(), OMSFeatureLifecycle {
    private var _config: T? = null
    val config: T
        get() = _config ?: throw IllegalStateException("Feature config is not initialized yet.")

    abstract val info: FeatureInfo

    override fun onOmsTick(event: OMSLifecycle.TickingEvent) {
        watchConfig()
        if (isConfigurationUpdated) {
            onConfigUpdated(event)
        }
    }

    override fun onOmsStarted(event: OMSLifecycle.StartingEvent) {}
    override fun onOmsStopping(event: OMSLifecycle.StoppingEvent) {}

    override fun onOmsRegisterConfig(config: FeatureConfig) {
        @Suppress("UNCHECKED_CAST")
        this._config = config as T
    }

    override fun onEnabled() {
        flagConfigAsDirty()
    }

    override fun onDisabled() {
        flagConfigAsDirty()
    }

    fun isEnabled(): Boolean = config.isEnabled()

    fun enable() {
        config.enable()
        onEnabled()
    }

    fun disable() {
        config.disable()
        onDisabled()
    }

    open fun getFeatureCommands(): List<OMSCommandEntry> = emptyList()
}