package io.conboi.oms.feature.autorestart.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.OperateMyServer
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.common.infrastructure.config.CServer
import io.conboi.oms.feature.autorestart.AutoRestartFeature
import io.conboi.oms.feature.autorestart.infrastructure.config.CAutoRestartFeature
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object AutoRestartOmsLifecycleEvents {

    @SubscribeEvent
    fun onRegisterFeatureEvent(event: OMSLifecycle.Feature.RegisterEvent) {
        val feature = AutoRestartFeature()
        OMSFeatureManagers.oms.register(feature)
    }

    @SubscribeEvent
    fun onRegisterFeatureConfigEvent(event: OMSLifecycle.Feature.RegisterConfigEvent) {
        val config = CServer.features.getFeatureConfig<CAutoRestartFeature>(CAutoRestartFeature.NAME)
        val feature = OMSFeatureManagers.oms.getFeatureById<AutoRestartFeature>(CAutoRestartFeature.NAME)
        feature?.onOmsRegisterConfig(config)
    }
}