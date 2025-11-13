package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.event

import io.conboi.oms.api.OmsAddons
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.ServerAccess
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.core.WatchDogEssentials
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = WatchDogEssentials.MOD_ID)
internal object EmptyServerRestartEvents {

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val addon = OmsAddons.get(WatchDogEssentials.MOD_ID)
        val feature = addon?.getFeatureById<EmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME)
        feature?.clearTime()
    }

    @SubscribeEvent
    fun onPlayerLoggedOut(event: PlayerEvent.PlayerLoggedOutEvent) {
        val server = ServerAccess.getCurrentServer()
        val playerCount = server.playerCount

        if (playerCount <= 1) { // because the player who just logged out is still counted in playerCount
            val addon = OmsAddons.get(WatchDogEssentials.MOD_ID)
            val feature =
                addon?.getFeatureById<EmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME)
            feature?.initTime()
        }
    }
}