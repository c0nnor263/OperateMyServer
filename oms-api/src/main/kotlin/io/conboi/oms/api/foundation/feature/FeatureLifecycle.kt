package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext

/**
 * Defines the lifecycle callbacks for an OMS feature.
 *
 * Implementations receive notifications about OMS lifecycle stages,
 * periodic ticking, configuration registration, and feature
 * enable/disable state changes.
 *
 * Lifecycle methods are invoked by the owning [io.conboi.oms.api.foundation.manager.FeatureManager]
 * according to the current server state.
 */
interface FeatureLifecycle {

    /**
     * Called on OMS ticking events.
     *
     * This method is invoked periodically according to the
     * tick frequency defined by the owning feature manager.
     *
     * Implementations should avoid long-running or blocking operations.
     *
     * @param event the ticking lifecycle event
     * @param context the addon context
     */
    fun onOmsTick(
        event: OMSLifecycle.TickingEvent,
        context: AddonContext
    )

    /**
     * Called when OMS is starting.
     *
     * This callback signals that the server and OMS runtime
     * are entering the active phase.
     *
     * @param event the starting lifecycle event
     * @param context the addon context
     */
    fun onOmsStarted(
        event: OMSLifecycle.StartingEvent,
        context: AddonContext
    ) {
    }

    /**
     * Called when OMS is stopping.
     *
     * This callback signals the final lifecycle phase and
     * should be used to release resources or persist state.
     *
     * @param event the stopping lifecycle event
     * @param context the addon context
     */
    fun onOmsStopping(
        event: OMSLifecycle.StoppingEvent,
        context: AddonContext
    ) {
    }

    /**
     * Called to register feature-specific configuration entries.
     *
     * This method is typically invoked during the OMS
     * configuration registration phase.
     */
    fun onOmsRegisterConfig()

    /**
     * Called when the feature is enabled.
     *
     * This callback is invoked after the feature transitions
     * to the enabled state.
     */
    fun onEnabled()

    /**
     * Called when the feature is disabled.
     *
     * This callback is invoked after the feature transitions
     * to the disabled state.
     */
    fun onDisabled()
}
