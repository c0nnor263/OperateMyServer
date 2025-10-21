package io.conboi.oms.utils.infrastructure.config

import io.conboi.oms.api.infrastructure.config.FeatureConfig

abstract class FeatureConfigImpl : ConfigBase(), FeatureConfig {
    val enabled = b(true, "enabled")

    override fun isEnabled(): Boolean = enabled.get()

    override fun enable() {
        enabled.set(true)
    }

    override fun disable() {
        enabled.set(false)
    }
}