package io.conboi.oms.core.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.OperateMyServer
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.core.content.StopManager
import io.conboi.oms.core.infrastructure.file.OMSPaths
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object OMSLifecycleListener {

    @SubscribeEvent
    fun onRegisterFeaturesEvent(event: OMSLifecycleInternal.Feature.RegisterEvent) {
        FORGE_BUS.post(OMSLifecycle.Feature.RegisterEvent())
        OMSFeatureManagers.runForEach {
            freeze()
        }
    }

    @SubscribeEvent
    fun onServerReadyEvent(event: OMSLifecycleInternal.Server.ReadyEvent) {
        StopManager.installHook()
        OMSPaths.init(event.server)

        FORGE_BUS.post(OMSLifecycle.Feature.RegisterConfigEvent())

        val startingEvent = OMSLifecycle.StartingEvent(event.server)
        OMSFeatureManagers.runForEach {
            onStartingEvent(startingEvent)
        }
        FORGE_BUS.post(startingEvent)
    }

    @SubscribeEvent
    fun onTickingEvent(event: OMSLifecycle.TickingEvent) {
        OMSFeatureManagers.runForEach {
            onTickingEvent(event)
        }
    }

    @SubscribeEvent
    fun onServerPreShutdownEvent(event: OMSLifecycleInternal.Server.PreShutdownEvent) {
        val stoppingEvent = OMSLifecycle.StoppingEvent(event.server)
        FORGE_BUS.post(stoppingEvent)
        OMSFeatureManagers.runForEach {
            onStoppingEvent(stoppingEvent)
        }
    }

    @SubscribeEvent
    fun onStopRequestedEvent(event: OMSLifecycle.StopRequestedEvent) {
        StopManager.stop(event)
    }
}