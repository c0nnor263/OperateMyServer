package io.conboi.oms.event.listener

import io.conboi.oms.OmsAddons
import io.conboi.oms.OperateMyServerAddon
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.event.OMSInternal
import io.conboi.oms.infrastructure.OmsStartLogger
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object OMSLifecycleListener {

    @SubscribeEvent
    fun onAddonRegisterEvent(event: OMSLifecycle.Addon.RegisterEvent) {
        event.registry.register(OperateMyServerAddon())
    }

    @SubscribeEvent
    fun onAddonPrepareEvent(event: OMSInternal.Addon.PrepareEvent) {
        val registryAddons = mutableListOf<OmsAddon>()
        val registerEvent = OMSLifecycle.Addon.RegisterEvent { addon ->
            registryAddons.add(addon)
        }
        FORGE_BUS.post(registerEvent)

        OmsAddons.onPrepare(registryAddons)
    }

    @SubscribeEvent
    fun onServerReadyEvent(event: OMSInternal.Server.ReadyEvent) {
        OmsAddons.onServerReady(event.server)

        val startingEvent = OMSLifecycle.StartingEvent(event.server)
        OmsAddons.onOmsStarted(startingEvent)
        FORGE_BUS.post(startingEvent)
        OmsStartLogger().showGreetings()
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