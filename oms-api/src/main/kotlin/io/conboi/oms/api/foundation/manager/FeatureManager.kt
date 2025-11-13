package io.conboi.oms.api.foundation.manager

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.TickTimer
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.info.InfoProvider
import io.conboi.oms.api.foundation.logging.LoggerProvider

abstract class FeatureManager(val modId: String) : InfoProvider<FeatureManagerInfo>, LoggerProvider {
    /**
     * The unique name of this feature manager. Defaults to "main"
     * Must be all lowercase, no spaces, only alphanumeric characters, underscores, hyphens and colons
     */
    open val name: String = "main"

    /**
     * The full ID of this feature manager in the format "modid:id". Needed for registration in OMSFeatureManagers
     * E.g. "oms:main"
     */
    val id: String get() = "$modId:$name"

    /**
     * The registry of features by their unique ID
     */
    protected val registry = mutableMapOf<String, OmsFeature<*>>()
    private var prioritizedFeatures: List<OmsFeature<*>> = emptyList()

    /**
     * Timer to control tick frequency for features.
     * Defaults to a new TickTimer instance with 20 tick interval (1 second)
     */
    protected open val tickTimer = TickTimer()

    /**
     * Whether the feature manager is frozen (no more features can be registered).
     */
    private var frozen = false

    override fun info(): FeatureManagerInfo = FeatureManagerInfo(
        id = id,
        modId = modId,
        name = name,
        featuresInfo = prioritizedFeatures.map(OmsFeature<*>::info)
    )

    /**
     * Registers a feature with the manager.
     * Must be called before the OMS's event [OMSLifecycle.StartingEvent].
     * @param feature The feature to register
     * @throws IllegalStateException if the manager is frozen
     * @throws IllegalArgumentException if a feature with the same ID is already registered
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
     * Freezes the feature manager, preventing any further feature registrations and sorting features by priority.
     * This should be called once, after all features have been registered and before the OMS's event [OMSLifecycle.StartingEvent].
     */
    fun freeze() {
        if (frozen) return
        frozen = true
        prioritizedFeatures = registry.values.sortedByDescending { it.info().priority }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : OmsFeature<*>> getFeatureById(id: String): T? {
        return registry[id] as? T?
    }

    open fun onStartingEvent(event: OMSLifecycle.StartingEvent) {
        prioritizedFeatures.forEach { feature ->
            feature.onOmsStarted(event)
        }
    }

    open fun onTickingEvent(event: OMSLifecycle.TickingEvent) {
        if (!tickTimer.shouldFire(event.server.tickCount)) return
        prioritizedFeatures.forEach { feature ->
            if (event.isServerStopping) return
            if (feature.isEnabled()) {
                feature.onOmsTick(event)
            }
        }
    }

    open fun onStoppingEvent(event: OMSLifecycle.StoppingEvent) {
        prioritizedFeatures.forEach { it.onOmsStopping(event) }
        registry.clear()
    }

    open fun onRegisterConfig() {
        prioritizedFeatures.forEach(OmsFeature<*>::onOmsRegisterConfig)
    }
}