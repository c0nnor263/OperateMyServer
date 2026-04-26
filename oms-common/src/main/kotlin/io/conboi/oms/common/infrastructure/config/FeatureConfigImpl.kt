package io.conboi.oms.common.infrastructure.config

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

    override fun getConfigData(): Map<String, Any> {
        val result = LinkedHashMap<String, Any>()

        for (cv in allValues) {
            // Skip config groups (they have no actual value)
            if (cv is ConfigGroup) continue

            val v = cv.get() ?: continue
            result[cv.name] = normalizeValue(v) ?: continue
        }

        return result
    }
}