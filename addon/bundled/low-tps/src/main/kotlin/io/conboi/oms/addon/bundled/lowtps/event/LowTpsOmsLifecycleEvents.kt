package io.conboi.oms.addon.bundled.lowtps.event

import io.conboi.oms.addon.bundled.lowtps.LowTpsFeature
import io.conboi.oms.addon.bundled.lowtps.infrastructure.config.CLowTpsFeature
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.OperateMyServer
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.core.infrastructure.config.CServer
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object LowTpsOmsLifecycleEvents {

    @SubscribeEvent
    fun onRegisterFeaturesEvent(event: OMSLifecycle.Feature.RegisterEvent) {
        val feature = LowTpsFeature()
        OMSFeatureManagers.oms.register(feature)
    }

    @SubscribeEvent
    fun onRegisterFeaturesConfigEvent(event: OMSLifecycle.Feature.RegisterConfigEvent) {
        val config = CServer.features.getFeatureConfig<CLowTpsFeature>(CLowTpsFeature.NAME)
        val feature = OMSFeatureManagers.oms.getFeatureById<LowTpsFeature>(CLowTpsFeature.NAME)
        feature?.onOmsRegisterConfig(config)
    }
}