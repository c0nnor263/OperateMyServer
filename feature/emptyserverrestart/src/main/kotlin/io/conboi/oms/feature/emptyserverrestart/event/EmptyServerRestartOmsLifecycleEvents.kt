package io.conboi.oms.feature.emptyserverrestart.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.OperateMyServer
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.common.infrastructure.config.CServer
import io.conboi.oms.feature.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.feature.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
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