package io.conboi.oms.watchdogessentials.feature.emptyserver.event

import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import io.conboi.oms.watchdogessentials.feature.emptyserver.EmptyServerFeature
import io.conboi.oms.watchdogessentials.feature.emptyserver.foundation.ServerAccess
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

internal class EmptyServerEvents(
    private val feature: EmptyServerFeature
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