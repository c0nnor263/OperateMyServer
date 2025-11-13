package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.info.InfoProvider
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.api.infrastructure.config.FeatureConfig


abstract class OmsFeature<T : FeatureConfig>(
    private val configProvider: ConfigProvider<T>
) : ConfigWatcher(), FeatureLifecycle, InfoProvider<FeatureInfo> {
    private var _config: T? = null
    val config: T
        get() = _config ?: throw IllegalStateException("Feature config is not initialized yet.")

    open val additionalCommands: List<OMSCommandEntry> = emptyList()

    override fun info(): FeatureInfo {
        return FeatureInfo(
            id = _config?.name ?: "",
            commands = additionalCommands,
            configInfo = _config?.info()
        )
    }

    override fun onOmsTick(event: OMSLifecycle.TickingEvent) {
        watchConfig()
        if (isConfigDirty) {
            onConfigUpdated(event)
        }
    }

    override fun onOmsRegisterConfig() {
        this._config = configProvider.get()
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
}