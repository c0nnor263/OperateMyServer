package io.conboi.oms.event

import io.conboi.oms.OMSFeatures
import io.conboi.oms.common.OMSFeatureManager
import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.common.content.StopManager
import io.conboi.oms.common.infrastructure.file.OMSPaths
import io.conboi.oms.infrastructure.config.OMSConfigs
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
object ServerLifecycleEvents {
    @SubscribeEvent
    fun onServerStarted(event: ServerStartedEvent) {
        StopManager.installHook()
        OMSPaths.init(event)

        OMSFeatures.registerAll(OMSConfigs.server)
        OMSFeatureManager.activateAll(event)
    }

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        OMSFeatureManager.onServerTick(event)
    }

    @SubscribeEvent
    fun onServerStopping(event: ServerStoppingEvent) {
        OMSFeatureManager.onServerStopping(event)
    }
}