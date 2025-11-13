package io.conboi.oms.event.listener

import io.conboi.oms.api.OmsAddons
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.content.StopManager
import io.conboi.oms.core.OperateMyServer
import io.conboi.oms.event.OMSInternal
import io.conboi.oms.infrastructure.OMSStartLogger
import io.conboi.oms.infrastructure.file.OMSRootPath
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object OMSLifecycleListener {

    @SubscribeEvent
    fun onFeaturePrepareEvent(event: OMSInternal.Feature.PrepareEvent) {
        OmsAddons.onRegisterFeatures()
        OmsAddons.freeze()
    }

    @SubscribeEvent
    fun onServerReadyEvent(event: OMSInternal.Server.ReadyEvent) {
        StopManager.installHook()
        OMSRootPath.init(event.server)
        OmsAddons.onInitializeOmsRoot(OMSRootPath.root)

        OmsAddons.onRegisterConfigs()

        val startingEvent = OMSLifecycle.StartingEvent(event.server)
        OmsAddons.onOmsStarted(startingEvent)
        FORGE_BUS.post(startingEvent)
        OMSStartLogger().showGreetings()
    }

    @SubscribeEvent
    fun onTickingEvent(event: OMSLifecycle.TickingEvent) {
        OmsAddons.onOmsTick(event)
    }

    @SubscribeEvent
    fun onServerPreShutdownEvent(event: OMSInternal.Server.PreShutdownEvent) {
        val stoppingEvent = OMSLifecycle.StoppingEvent(event.server)
        FORGE_BUS.post(stoppingEvent)
        OmsAddons.onOmsStopping(stoppingEvent)
    }
}