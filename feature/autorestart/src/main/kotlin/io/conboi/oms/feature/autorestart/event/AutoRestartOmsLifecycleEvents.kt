package io.conboi.oms.feature.autorestart.event

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.common.OMSFeatureManager
import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.common.infrastructure.config.CServer
import io.conboi.oms.feature.autorestart.AutoRestartFeature
import io.conboi.oms.feature.autorestart.foundation.AutoRestartFeatureType
import io.conboi.oms.feature.autorestart.infrastructure.config.CAutoRestartFeature
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object AutoRestartOmsLifecycleEvents {

    @SubscribeEvent
    fun onRegisterFeaturesEvent(event: OMSLifecycle.RegisterFeaturesEvent) {
        val info = FeatureInfo(
            type = AutoRestartFeatureType,
            priority = FeatureInfo.Priority.COMMON
        )
        val feature = AutoRestartFeature(info)
        event.registry.register(feature)
    }

    @SubscribeEvent
    fun onRegisterFeaturesConfigEvent(event: OMSLifecycle.RegisterFeaturesConfigEvent) {
        val config = CServer.features.getFeatureConfig<CAutoRestartFeature>(CAutoRestartFeature.NAME)
        val feature = OMSFeatureManager.getFeatureByType<AutoRestartFeature>(AutoRestartFeatureType)
        feature?.onOmsRegisterConfig(config)
    }
}