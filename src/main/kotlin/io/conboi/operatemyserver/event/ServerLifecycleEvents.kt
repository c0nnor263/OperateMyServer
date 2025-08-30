package io.conboi.operatemyserver.event

import io.conboi.operatemyserver.OMSFeatureManager
import io.conboi.operatemyserver.OMSFeatures
import io.conboi.operatemyserver.common.OperateMyServer
import io.conboi.operatemyserver.common.content.StopManager
import io.conboi.operatemyserver.common.infrastructure.file.OMSPaths
import io.conboi.operatemyserver.infrastructure.config.OMSConfigs
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
object ServerLifecycleEvents {
    @SubscribeEvent
    fun onServerStarted(event: ServerStartedEvent) {
        StopManager.installHook()
        OMSPaths.init()

        val features = OMSFeatures.createAll(OMSConfigs.server)
        OMSFeatureManager.activateAll(features, event)
    }

    @SubscribeEvent
    fun onServerStopping(event: ServerStoppingEvent) {
        OMSFeatureManager.onServerStopping(event)
    }
}