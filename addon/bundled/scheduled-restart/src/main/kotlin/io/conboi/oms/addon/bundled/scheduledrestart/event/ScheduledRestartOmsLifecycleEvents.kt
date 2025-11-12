package io.conboi.oms.addon.bundled.scheduledrestart.event

import io.conboi.oms.addon.bundled.scheduledrestart.ScheduledRestartFeature
import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.OperateMyServer
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.core.infrastructure.config.CServer
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object ScheduledRestartOmsLifecycleEvents {

    @SubscribeEvent
    fun onRegisterFeatureEvent(event: OMSLifecycle.Feature.RegisterEvent) {
        val feature = ScheduledRestartFeature()
        OMSFeatureManagers.oms.register(feature)
    }

    @SubscribeEvent
    fun onRegisterFeatureConfigEvent(event: OMSLifecycle.Feature.RegisterConfigEvent) {
        val config =
            CServer.features.getFeatureConfig<CScheduledRestartFeature>(CScheduledRestartFeature.Companion.NAME)
        val feature =
            OMSFeatureManagers.oms.getFeatureById<ScheduledRestartFeature>(CScheduledRestartFeature.Companion.NAME)
        feature?.onOmsRegisterConfig(config)
    }
}