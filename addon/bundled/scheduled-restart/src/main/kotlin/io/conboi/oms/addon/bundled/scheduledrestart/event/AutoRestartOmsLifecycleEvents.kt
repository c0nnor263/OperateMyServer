package io.conboi.oms.addon.bundled.scheduledrestart.event

import io.conboi.oms.addon.bundled.scheduledrestart.AutoRestartFeature
import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CAutoRestartFeature
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.OperateMyServer
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.core.infrastructure.config.CServer
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
        val config = CServer.features.getFeatureConfig<CAutoRestartFeature>(CAutoRestartFeature.Companion.NAME)
        val feature = OMSFeatureManagers.oms.getFeatureById<AutoRestartFeature>(CAutoRestartFeature.Companion.NAME)
        feature?.onOmsRegisterConfig(config)
    }
}