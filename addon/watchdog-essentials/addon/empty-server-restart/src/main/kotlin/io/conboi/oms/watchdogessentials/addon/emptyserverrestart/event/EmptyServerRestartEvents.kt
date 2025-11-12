package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.ServerAccess
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.core.WatchDogEssentials
import io.conboi.oms.watchdogessentials.core.foundation.feature.we
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = WatchDogEssentials.MOD_ID)
internal object EmptyServerRestartEvents {

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val feature = OMSFeatureManagers.we.getFeatureById<EmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME)
        feature?.clearTime()
    }

    @SubscribeEvent
    fun onPlayerLoggedOut(event: PlayerEvent.PlayerLoggedOutEvent) {
        val server = ServerAccess.getCurrentServer()
        val playerCount = server.playerCount

        println("Player logged out, current player count: $playerCount")
        if (playerCount <= 1) { // because the player who just logged out is still counted in playerCount
            val feature =
                OMSFeatureManagers.we.getFeatureById<EmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME)
            feature?.initTime()
        }
    }
}