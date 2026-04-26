package io.conboi.oms.api.foundation.addon

/**
 * Registry used to register [OmsAddon] instances during the OMS addon lifecycle.
 *
 * Implementations of this interface are provided by OMS and exposed
 * through the [io.conboi.oms.api.event.OMSLifecycle.Addon.RegisterEvent].
 *
 * Addons must be registered exclusively during this lifecycle phase.
 * Registering addons outside of this event is not supported and may
 * result in undefined behavior.
 */
fun interface AddonRegistry {

    /**
     * Registers an addon with OMS.
     *
     * @param addon the addon instance to register
     */
    fun register(addon: OmsAddon)
}
