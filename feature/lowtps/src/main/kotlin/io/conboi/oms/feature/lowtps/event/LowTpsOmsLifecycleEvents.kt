package io.conboi.oms.feature.lowtps.event

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.common.OMSFeatureManager
import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.common.infrastructure.config.CServer
import io.conboi.oms.feature.lowtps.LowTpsFeature
import io.conboi.oms.feature.lowtps.foundation.LowTpsFeatureType
import io.conboi.oms.feature.lowtps.infrastructure.config.CLowTpsFeature
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object LowTpsOmsLifecycleEvents {

    @SubscribeEvent
    fun onRegisterFeaturesEvent(event: OMSLifecycle.RegisterFeaturesEvent) {
        val info = FeatureInfo(
            type = LowTpsFeatureType,
            priority = FeatureInfo.Priority.CRITICAL
        )
        val feature = LowTpsFeature(info)
        event.registry.register(feature)
    }

    @SubscribeEvent
    fun onRegisterFeaturesConfigEvent(event: OMSLifecycle.RegisterFeaturesConfigEvent) {
        val config = CServer.features.getFeatureConfig<CLowTpsFeature>(CLowTpsFeature.NAME)
        val feature = OMSFeatureManager.getFeatureByType<LowTpsFeature>(LowTpsFeatureType)
        feature?.onOmsRegisterConfig(config)
    }
}