package io.conboi.oms.api.event

import io.conboi.oms.api.foundation.addon.AddonRegistry
import net.minecraft.server.MinecraftServer
import net.minecraftforge.eventbus.api.Event

/**
 * Base class for OMS lifecycle events.
 *
 * Lifecycle events represent distinct stages in the runtime lifecycle
 * of the OMS-controlled Minecraft server.
 *
 * These events are informational and state-oriented, allowing addons
 * and internal components to react to server state transitions without
 * directly triggering actions.
 */
abstract class OMSLifecycle : Event() {

    /**
     * Events related to addon discovery and registration.
     */
    object Addon {

        /**
         * Fired during the addon registration phase.
         *
         * This event provides access to the [AddonRegistry] and allows
         * addons to be registered before the server reaches its
         * operational state.
         *
         * No addon registrations should occur after this phase.
         *
         * @property registry the addon registry used for registration
         */
        data class RegisterEvent(
            val registry: AddonRegistry,
        ) : OMSLifecycle()
    }

    /**
     * Fired when the Minecraft server is starting and OMS initialization begins.
     *
     * At this stage, core OMS systems are being prepared, but gameplay
     * logic and features may not yet be active.
     *
     * @property server the Minecraft server instance
     */
    data class StartingEvent(
        val server: MinecraftServer,
    ) : OMSLifecycle()

    /**
     * Fired on each server tick while the server is running.
     *
     * This event may also be emitted during shutdown, in which case
     * [isServerStopping] will be set to `true`.
     *
     * @property server the Minecraft server instance
     * @property isServerStopping whether the server is currently shutting down
     */
    data class TickingEvent(
        val server: MinecraftServer,
        val isServerStopping: Boolean
    ) : OMSLifecycle()

    /**
     * Fired when the server is stopping.
     *
     * This event signals the final phase of the server lifecycle and
     * should be used to release resources, flush state, or perform
     * graceful shutdown logic.
     *
     * @property server the Minecraft server instance
     */
    data class StoppingEvent(
        val server: MinecraftServer,
    ) : OMSLifecycle()
}
