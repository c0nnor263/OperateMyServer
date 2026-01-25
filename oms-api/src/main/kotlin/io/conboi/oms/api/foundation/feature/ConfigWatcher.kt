package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.CachedField

/**
 * Base class for observing configuration changes within OMS features.
 *
 * [ConfigWatcher] provides a lightweight mechanism for tracking derived
 * configuration values and detecting when configuration state changes.
 *
 * Intended primarily for use within [OmsFeature] implementations.
 * Advanced usage outside features is possible but requires explicit lifecycle integration.
 */
abstract class ConfigWatcher {

    /**
     * Registered configuration fields being observed.
     */
    private val configFields = mutableListOf<CachedField<*, *>>()

    /**
     * Indicates whether the configuration state has changed
     * since the last update cycle.
     */
    var isConfigDirty: Boolean = false
        protected set

    /**
     * Marks the configuration state as dirty.
     *
     * This method may be invoked by observed fields or
     * custom validation logic when configuration changes.
     */
    open fun markConfigAsDirty() {
        isConfigDirty = true
    }

    /**
     * Handles configuration updates during OMS ticking.
     *
     * Implementations may override this method to react
     * to configuration changes once per update cycle.
     *
     * The default implementation clears the dirty flag.
     *
     * @param event the ticking lifecycle event
     */
    open fun onConfigUpdated(event: OMSLifecycle.TickingEvent) {
        isConfigDirty = false
    }

    /**
     * Evaluates all registered configuration fields and
     * checks for updates.
     *
     * This method triggers observation on all fields
     * configured with [CachedField.observe].
     */
    protected fun watchConfig() {
        configFields.forEach { it.watch() }
    }

    /**
     * Registers a configuration field to be observed for changes.
     *
     * The provided builder block is used to configure a [CachedField].
     * Observation is automatically enabled for the field.
     *
     * @param block configuration block defining the cached field
     * @return the registered configuration field
     */
    protected fun <K, V> configField(
        block: CachedField.Builder<K, V>.() -> Unit
    ): CachedField<K, V> {
        val builder = CachedField.Builder<K, V>().apply(block)
        builder.observe = true

        val field = builder.build()
        configFields.add(field)

        return field
    }
}
