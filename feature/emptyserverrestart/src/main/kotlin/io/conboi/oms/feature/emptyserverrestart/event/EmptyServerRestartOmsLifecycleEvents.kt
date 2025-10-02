package io.conboi.oms.feature.emptyserverrestart.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.OperateMyServer
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.common.infrastructure.config.CServer
import io.conboi.oms.feature.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.feature.emptyserverrestart.foundation.EmptyServerRestartFeatureType
import io.conboi.oms.feature.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object EmptyServerRestartOmsLifecycleEvents {

    @SubscribeEvent
    fun onRegisterFeaturesEvent(event: OMSLifecycle.RegisterFeaturesEvent) {
        val info = FeatureInfo(
            type = EmptyServerRestartFeatureType,
            priority = FeatureInfo.Priority.COMMON
        )
        val feature = EmptyServerRestartFeature(info)
        OMSFeatureManagers.oms.register(feature)
    }

    @SubscribeEvent
    fun onRegisterFeaturesConfigEvent(event: OMSLifecycle.RegisterFeaturesConfigEvent) {
        val config = CServer.features.getFeatureConfig<CEmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME)
        val feature = OMSFeatureManagers.oms.getFeatureByType<EmptyServerRestartFeature>(EmptyServerRestartFeatureType)
        feature?.onOmsRegisterConfig(config)
    }
}