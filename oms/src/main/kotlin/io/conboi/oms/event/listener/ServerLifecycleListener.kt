package io.conboi.oms.event.listener

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.content.StopManager
import io.conboi.oms.event.OMSInternal
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object ServerLifecycleListener {
    @SubscribeEvent
    fun onServerStarted(event: ServerStartedEvent) {
        FORGE_BUS.post(OMSInternal.Server.ReadyEvent(event.server))
    }

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        FORGE_BUS.post(
            OMSLifecycle.TickingEvent(
                server = event.server,
                isServerStopping = StopManager.isServerStopping()
            )
        )
    }

    @SubscribeEvent
    fun onServerStopping(event: ServerStoppingEvent) {
        FORGE_BUS.post(OMSInternal.Server.PreShutdownEvent(event.server))
    }
}