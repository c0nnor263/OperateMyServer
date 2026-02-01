package io.conboi.oms.api.event

import io.conboi.oms.api.foundation.reason.StopReason
import net.minecraft.server.MinecraftServer
import net.minecraftforge.eventbus.api.Event

/**
 * Base class for OMS action events.
 *
 * Action events represent explicit requests for OMS to perform
 * a concrete operation (for example, stopping or restarting the server).
 *
 * Unlike lifecycle events, action events are intent-based and
 * may be emitted by features, addons, or internal OMS components
 * to trigger controlled system behavior.
 */
abstract class OMSActions : Event() {

    /**
     * Fired when a server stop operation is explicitly requested.
     *
     * This event does not immediately stop the server by itself.
     * Instead, it signals OMS to evaluate the request and perform
     * a controlled shutdown or restart sequence based on the provided reason.
     *
     * @property server the Minecraft server instance
     * @property reason the logical reason for the stop request
     */
    data class StopRequestedEvent(
        val server: MinecraftServer,
        val reason: StopReason,
    ) : OMSActions()
}
