package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.event

import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.ServerAccess
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

internal class EmptyServerRestartEvents(
    private val feature: EmptyServerRestartFeature
) {
    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        feature.clearTime()
        LOG.debug("Player logged in, cleared empty server timer.")
    }

    @SubscribeEvent
    fun onPlayerLoggedOut(event: PlayerEvent.PlayerLoggedOutEvent) {
        val server = ServerAccess.getCurrentServer()
        val playerCount = server.playerCount

        if (playerCount <= 1) { // because the player who just logged out is still counted in playerCount
            feature.initTime()
            LOG.debug("Last player logged out, initialized empty server timer.")
        }
    }
}