package io.conboi.operatemyserver.event

import io.conboi.operatemyserver.OMSFeatureManager
import io.conboi.operatemyserver.common.OperateMyServer
import io.conboi.operatemyserver.common.foundation.TickTimer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.server.ServerLifecycleHooks

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
object TickEvents {
    private val tickTimer = TickTimer()

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        val server = ServerLifecycleHooks.getCurrentServer() ?: return
        if (!tickTimer.shouldFire(server.tickCount)) return
        OMSFeatureManager.onServerTick(event)
    }
}