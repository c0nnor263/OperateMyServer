package io.conboi.oms.watchdogessentials.event

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.watchdogessentials.WatchDogEssentialsAddon
import io.conboi.oms.watchdogessentials.common.WatchDogEssentials
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = WatchDogEssentials.MOD_ID)
internal object OMSLifecycleListener {
    @SubscribeEvent
    fun onAddonRegisterEvent(event: OMSLifecycle.Addon.RegisterEvent) {
        event.registry.register(WatchDogEssentialsAddon())
    }
}