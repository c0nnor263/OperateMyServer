package io.conboi.oms.watchdogessentials.addon.lowtps.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.watchdogessentials.addon.lowtps.LowTpsFeature
import io.conboi.oms.watchdogessentials.addon.lowtps.infrastructure.config.CLowTpsFeature
import io.conboi.oms.watchdogessentials.core.WatchDogEssentials
import io.conboi.oms.watchdogessentials.core.foundation.feature.we
import io.conboi.oms.watchdogessentials.core.infrastructure.config.CServer
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = WatchDogEssentials.MOD_ID)
internal object LowTpsOmsLifecycleEvents {

    @SubscribeEvent
    fun onRegisterFeaturesEvent(event: OMSLifecycle.Feature.RegisterEvent) {
        val feature = LowTpsFeature()
        OMSFeatureManagers.we.register(feature)
    }

    @SubscribeEvent
    fun onRegisterFeaturesConfigEvent(event: OMSLifecycle.Feature.RegisterConfigEvent) {
        val config = CServer.features.getFeatureConfig<CLowTpsFeature>(CLowTpsFeature.NAME)
        val feature = OMSFeatureManagers.we.getFeatureById<LowTpsFeature>(CLowTpsFeature.NAME)
        feature?.onOmsRegisterConfig(config)
    }
}