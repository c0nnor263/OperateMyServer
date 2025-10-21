package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.core.WatchDogEssentials
import io.conboi.oms.watchdogessentials.core.infrastructure.config.CServer
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = WatchDogEssentials.MOD_ID)
internal object EmptyServerRestartOmsLifecycleEvents {

    @SubscribeEvent
    fun onRegisterFeaturesEvent(event: OMSLifecycle.Feature.RegisterEvent) {
        val feature = EmptyServerRestartFeature()
        OMSFeatureManagers.oms.register(feature)
    }

    @SubscribeEvent
    fun onRegisterFeaturesConfigEvent(event: OMSLifecycle.Feature.RegisterConfigEvent) {
        val config = CServer.features.getFeatureConfig<CEmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME)
        val feature = OMSFeatureManagers.oms.getFeatureById<EmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME)
        feature?.onOmsRegisterConfig(config)
    }
}