package io.conboi.oms.api.foundation.manager

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.TickTimer
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.info.InfoProvider

/**
 * Manages a collection of OMS features belonging to a single addon.
 *
 * A feature manager is responsible for:
 * - registering features before server startup,
 * - freezing the feature set once initialization is complete,
 * - dispatching lifecycle events to features,
 * - controlling feature ticking frequency,
 * - exposing feature-related diagnostic information.
 *
 * Each addon owns at least one feature manager, uniquely identified
 * by its [id].
 *
 * @property addonId the identifier of the addon this manager belongs to
 */
abstract class FeatureManager(
    val addonId: String
) : InfoProvider<FeatureManagerInfo> {

    /**
     * Logical name of this feature manager.
     *
     * Defaults to `"main"`.
     *
     * Naming rules:
     * - lowercase only
     * - no spaces
     * - allowed characters: alphanumeric, `_`, `-`, `:`
     */
    open val name: String = "main"

    /**
     * Fully qualified identifier of this feature manager.
     *
     * Format: `<addonId>:<name>`
     *
     * Example:
     * ```
     * oms:main
     * ```
     */
    val id: String
        get() = "$addonId:$name"

    /**
     * Internal registry of features keyed by feature ID.
     */
    protected val registry = mutableMapOf<String, OmsFeature<*>>()

    /**
     * List of registered features sorted by priority (highest to lowest).
     *
     * This list is initialized when the manager is frozen and remains
     * immutable afterward.
     */
    private var prioritizedFeatures: List<OmsFeature<*>> = emptyList()

    /**
     * Timer controlling how often feature tick callbacks are executed.
     *
     * Defaults to a [TickTimer] with a 20-tick interval (1 second).
     */
    protected open val tickTimer = TickTimer()

    /**
     * Whether the feature manager is frozen.
     *
     * Once frozen, no further feature registrations are allowed.
     */
    private var frozen = false

    /**
     * Returns a snapshot of information about this feature manager.
     */
    override fun info(): FeatureManagerInfo = FeatureManagerInfo(
        id = id,
        addonId = addonId,
        name = name,
        featuresInfo = prioritizedFeatures.map(OmsFeature<*>::info)
    )

    /**
     * Returns the list of registered features in priority order.
     */
    fun features(): List<OmsFeature<*>> = prioritizedFeatures

    /**
     * Registers a feature with this manager.
     *
     * Feature registration is only allowed before the manager is frozen.
     *
     * @param feature the feature instance to register
     *
     * @throws IllegalStateException if the manager has already been frozen
     * @throws IllegalArgumentException if a feature with the same ID
     *         is already registered
     */
    fun register(feature: OmsFeature<*>) {
        check(!frozen) { "Cannot register features after server has started!" }

        val id = feature.info().id
        require(!registry.containsKey(id)) {
            "Feature '$id' already registered"
        }

        registry[id] = feature
    }

    /**
     * Freezes the feature manager.
     *
     * After freezing:
     * - no new features may be registered,
     * - feature order is fixed based on priority,
     * - lifecycle events may be dispatched.
     */
    fun freeze() {
        if (frozen) return
        frozen = true
        prioritizedFeatures = registry.values
            .sortedByDescending { it.info().priority }
    }

    /**
     * Retrieves a feature by its ID.
     *
     * @param id the feature identifier
     * @return the feature instance, or `null` if not found
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : OmsFeature<*>> getFeatureById(id: String): T? {
        return registry[id] as? T?
    }

    /**
     * Dispatches the OMS starting lifecycle event to all registered features.
     */
    open fun onStartingEvent(
        event: OMSLifecycle.StartingEvent,
        context: AddonContext
    ) {
        prioritizedFeatures.forEach { feature ->
            feature.onOmsStarted(event, context)
        }
    }

    /**
     * Dispatches ticking events to enabled features.
     *
     * Ticking frequency is controlled by [tickTimer].
     *
     * By default, no feature tick callbacks are executed while the server
     * is stopping.
     *
     * Implementations may override this method to customize ticking behavior
     * during shutdown.
     */
    open fun onTickingEvent(
        event: OMSLifecycle.TickingEvent,
        context: AddonContext
    ) {
        if (!tickTimer.shouldFire(event.server.tickCount)) return

        prioritizedFeatures.forEach { feature ->
            if (event.isServerStopping) return
            if (feature.isEnabled()) {
                feature.onOmsTick(event, context)
            }
        }
    }

    /**
     * Dispatches the OMS stopping lifecycle event to all features.
     *
     * After dispatching, the internal registry is cleared.
     */
    open fun onStoppingEvent(
        event: OMSLifecycle.StoppingEvent,
        context: AddonContext
    ) {
        prioritizedFeatures.forEach {
            it.onOmsStopping(event, context)
        }
        registry.clear()
    }

    /**
     * Triggers feature configuration registration callbacks.
     *
     * This method is typically invoked during the OMS configuration
     * registration phase.
     */
    open fun onRegisterConfig() {
        prioritizedFeatures.forEach(OmsFeature<*>::onOmsRegisterConfig)
    }
}
