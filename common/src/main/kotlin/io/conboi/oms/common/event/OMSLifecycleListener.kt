package io.conboi.oms.common.event

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.common.OMSFeatureManager
import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.common.content.StopManager
import io.conboi.oms.common.infrastructure.file.OMSPaths
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
object OMSLifecycleListener {

    @SubscribeEvent
    fun onRegisterFeaturesEvent(event: OMSLifecycleInternal.RegisterFeaturesEvent) {
        FORGE_BUS.post(OMSLifecycle.RegisterFeaturesEvent(OMSFeatureManager))
    }

    @SubscribeEvent
    fun onServerReadyEvent(event: OMSLifecycleInternal.ServerReadyEvent) {
        StopManager.installHook()
        OMSPaths.init(event.server)

        FORGE_BUS.post(OMSLifecycle.RegisterFeaturesConfigEvent())

        OMSFeatureManager.freeze()

        val startingEvent = OMSLifecycle.StartingEvent(event.server)
        OMSFeatureManager.onStartingEvent(startingEvent)
        FORGE_BUS.post(startingEvent)
    }

    @SubscribeEvent
    fun onTickingEvent(event: OMSLifecycle.TickingEvent) {
        OMSFeatureManager.onServerTick(event)
    }

    @SubscribeEvent
    fun onServerPreShutdownEvent(event: OMSLifecycleInternal.ServerPreShutdownEvent) {
        val stoppingEvent = OMSLifecycle.StoppingEvent(event.server)
        FORGE_BUS.post(stoppingEvent)
        OMSFeatureManager.onServerStopping(stoppingEvent)
    }
}