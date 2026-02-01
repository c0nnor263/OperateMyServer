package io.conboi.oms.api.foundation.addon

/**
 * Base class for defining an OMS addon.
 *
 * Addons represent modular extensions of OMS functionality and are
 * discovered and initialized during the OMS lifecycle.
 *
 * Developers should extend this class to implement custom behavior,
 * feature registration, and addon-specific configuration.
 *
 * @param id the unique identifier of the addon
 */
abstract class OmsAddon(val id: String) {

    /**
     * Configures the [AddonContextSpec] for this addon.
     *
     * Implementations may override this method to customize individual
     * components of the addon context (such as paths or feature managers).
     *
     * It is recommended to copy the provided [spec] and modify only the
     * required fields to preserve default OMS behavior.
     *
     * @param spec the default context specification
     * @return the configured context specification
     */
    open fun configureContext(spec: AddonContextSpec): AddonContextSpec = spec

    /**
     * Called during the addon registration phase to allow feature registration.
     *
     * At this stage, the addon context is fully initialized and can be used
     * to register features via the provided [AddonContext].
     *
     * @param context the initialized addon context
     */
    open fun onRegisterFeatures(context: AddonContext) {}
}
