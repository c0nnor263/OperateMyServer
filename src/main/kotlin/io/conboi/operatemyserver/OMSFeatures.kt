package io.conboi.operatemyserver

import io.conboi.operatemyserver.common.foundation.feature.FeatureInfo
import io.conboi.operatemyserver.common.foundation.feature.OmsFeature
import io.conboi.operatemyserver.common.infrastructure.config.FeatureConfigBase
import io.conboi.operatemyserver.feature.autorestart.AutoRestartFeature
import io.conboi.operatemyserver.feature.autorestart.infrastructure.config.CAutoRestartFeature
import io.conboi.operatemyserver.feature.lowtps.LowTpsFeature
import io.conboi.operatemyserver.feature.lowtps.infrastructure.config.CLowTpsFeature
import io.conboi.operatemyserver.infrastructure.config.CServer
import java.util.concurrent.ConcurrentHashMap
//
// TODO: Create verify feature fields
object OMSFeatures {
    private val entries = ConcurrentHashMap<FeatureInfo.Type, (CServer) -> OmsFeature<*>>()

    fun createAll(config: CServer): List<OmsFeature<*>> =
        entries.map { (_, factory) ->
            factory(config)
        }

    private inline fun <reified T : FeatureConfigBase> register(
        type: FeatureInfo.Type,
        crossinline factory: (T) -> OmsFeature<*>
    ) {
        require(!entries.containsKey(type)) { "Feature '$type' already registered" }
        entries[type] = { serverConfig ->
            val featureConfig: T = serverConfig.features.getConfigByFeatureType(type)
            factory(featureConfig)
        }
    }

    fun init() {
        register<CAutoRestartFeature>(FeatureInfo.Type.AUTO_RESTART, ::AutoRestartFeature)
        register<CLowTpsFeature>(FeatureInfo.Type.LOW_TPS, ::LowTpsFeature)
    }
}