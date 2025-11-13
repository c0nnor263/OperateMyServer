package io.conboi.oms.api.infrastructure.config

import io.conboi.oms.api.foundation.info.InfoProvider

interface FeatureConfig : InfoProvider<FeatureConfigInfo> {
    val name: String
    fun isEnabled(): Boolean
    fun enable()
    fun disable()
    fun getConfigData(): Map<String, Any> = emptyMap()

    override fun info(): FeatureConfigInfo = FeatureConfigInfo(
        name = name,
        isEnabled = isEnabled(),
        data = getConfigData()
    )
}