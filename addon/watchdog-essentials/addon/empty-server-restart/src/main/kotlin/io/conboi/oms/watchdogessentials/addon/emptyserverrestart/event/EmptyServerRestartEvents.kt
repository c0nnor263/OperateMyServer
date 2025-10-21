package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.core.WatchDogEssentials
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.server.ServerLifecycleHooks

@Mod.EventBusSubscriber(modid = WatchDogEssentials.MOD_ID)
internal object EmptyServerRestartEvents {

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val feature = OMSFeatureManagers.oms.getFeatureById<EmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME)
        feature?.clearTime()
        println("Player logged in, cleared empty server timer $feature")
    }

    @SubscribeEvent
    fun onPlayerLoggedOut(event: PlayerEvent.PlayerLoggedOutEvent) {
        val server = ServerLifecycleHooks.getCurrentServer()
        val playerCount = server.playerCount
        println("Player logged out, current player count: $playerCount")

        // TODO: Maybe introduce some options regarding player count threshold
        if (playerCount <= 1) { // because the player who just logged out is still counted in playerCount
            val feature =
                OMSFeatureManagers.oms.getFeatureById<EmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME)
            feature?.initTime()
            println("No players online, started empty server timer $feature")
        }
    }
}