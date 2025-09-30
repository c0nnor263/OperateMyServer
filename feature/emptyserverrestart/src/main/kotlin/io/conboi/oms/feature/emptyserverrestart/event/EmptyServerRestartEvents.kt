package io.conboi.oms.feature.emptyserverrestart.event

import io.conboi.oms.common.OMSFeatureManager
import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.feature.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.feature.emptyserverrestart.foundation.EmptyServerRestartFeatureType
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.server.ServerLifecycleHooks

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object EmptyServerRestartEvents {

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val feature = OMSFeatureManager.getFeatureByType<EmptyServerRestartFeature>(EmptyServerRestartFeatureType)
        feature?.clearTime()
    }

    @SubscribeEvent
    fun onPlayerLoggedOut(event: PlayerEvent.PlayerLoggedOutEvent) {
        val server = ServerLifecycleHooks.getCurrentServer()
        val playerCount = server.playerList.players.size

        // TODO: Maybe introduce some options regarding player count threshold
        if (playerCount == 0) {
            val feature = OMSFeatureManager.getFeatureByType<EmptyServerRestartFeature>(EmptyServerRestartFeatureType)
            feature?.initTime()
        }
    }
}