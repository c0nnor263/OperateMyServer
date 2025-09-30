package io.conboi.oms.event

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.common.event.OMSLifecycleInternal
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
object ServerLifecycleEvents {
    @SubscribeEvent
    fun onServerStarted(event: ServerStartedEvent) {
        FORGE_BUS.post(OMSLifecycleInternal.ServerReadyEvent(event.server))
    }

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        FORGE_BUS.post(OMSLifecycle.TickingEvent(event.server))
    }

    @SubscribeEvent
    fun onServerStopping(event: ServerStoppingEvent) {
        FORGE_BUS.post(OMSLifecycleInternal.ServerPreShutdownEvent(event.server))
    }
}