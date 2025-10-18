package io.conboi.oms.core.infrastructure.config

import io.conboi.oms.api.infrastructure.config.FeatureConfig

object CFeatures : ConfigBase() {
    override val name: String = "features"

    private val featureConfigs: MutableMap<String, FeatureConfig> = mutableMapOf()

    fun registerFeatureConfig(factory: CFeatures.() -> FeatureConfig) {
        val config = factory()
        featureConfigs[config.name] = config
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : FeatureConfig> getFeatureConfig(name: String): T {
        return featureConfigs[name] as T
    }
}

