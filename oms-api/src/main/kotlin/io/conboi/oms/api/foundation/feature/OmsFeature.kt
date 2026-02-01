package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.info.InfoProvider
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.api.infrastructure.config.FeatureConfig

/**
 * Base class for OMS features.
 *
 * [OmsFeature] combines feature lifecycle handling, configuration management,
 * command integration, and diagnostic information exposure into a single
 * extensible abstraction.
 *
 * Features are registered through a [io.conboi.oms.api.foundation.manager.FeatureManager], participate in
 * OMS lifecycle events, and may be dynamically enabled or disabled
 * at runtime.
 *
 * @param T the type of the feature configuration
 * @property configProvider provider responsible for supplying the feature configuration
 */
abstract class OmsFeature<T : FeatureConfig>(
    private val configProvider: ConfigProvider<T>
) : ConfigWatcher(),
    FeatureLifecycle,
    InfoProvider<FeatureInfo> {

    private var _config: T? = null

    /**
     * The active feature configuration.
     *
     * This value is initialized during the configuration registration phase
     * and is guaranteed to be available after [onOmsRegisterConfig] is called.
     *
     * @throws IllegalStateException if accessed before initialization
     */
    val config: T
        get() = _config
            ?: throw IllegalStateException("Feature config is not initialized yet.")

    /**
     * Additional commands exposed by this feature.
     *
     * These commands are registered alongside the owning feature manager
     * and become available when the feature is active.
     */
    open val additionalCommands: List<OMSCommandEntry> = emptyList()

    /**
     * Creates event listeners to be registered on Forge's event bus.
     *
     * This allows features to subscribe to external events outside
     * the OMS lifecycle model.
     *
     * @return a list of event listener instances
     */
    open fun createEventListeners(): List<Any> = emptyList()

    /**
     * Returns a snapshot of information describing this feature.
     *
     * The returned information is used for registration, diagnostics,
     * and status reporting.
     */
    override fun info(): FeatureInfo {
        return FeatureInfo(
            id = _config?.name ?: "",
            additionalCommands = additionalCommands,
            configInfo = _config?.info()
        )
    }

    /**
     * Handles OMS ticking events.
     *
     * This implementation observes configuration fields and triggers
     * configuration update handling when changes are detected.
     */
    override fun onOmsTick(
        event: OMSLifecycle.TickingEvent,
        context: AddonContext
    ) {
        watchConfig()
        if (isConfigDirty) {
            onConfigUpdated(event)
        }
    }

    /**
     * Initializes the feature configuration.
     *
     * This method is invoked during the OMS configuration registration phase.
     */
    override fun onOmsRegisterConfig() {
        this._config = configProvider.get()
    }

    /**
     * Called when the feature transitions to the enabled state.
     *
     * The default implementation marks the configuration as dirty.
     */
    override fun onEnabled() {
        markConfigAsDirty()
    }

    /**
     * Called when the feature transitions to the disabled state.
     *
     * The default implementation marks the configuration as dirty.
     */
    override fun onDisabled() {
        markConfigAsDirty()
    }

    /**
     * Returns whether the feature is currently enabled.
     */
    fun isEnabled(): Boolean = config.isEnabled()

    /**
     * Enables the feature.
     *
     * This updates the configuration state and triggers
     * the [onEnabled] callback.
     */
    fun enable() {
        config.enable()
        onEnabled()
    }

    /**
     * Disables the feature.
     *
     * This updates the configuration state and triggers
     * the [onDisabled] callback.
     */
    fun disable() {
        config.disable()
        onDisabled()
    }
}
