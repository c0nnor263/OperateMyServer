package io.conboi.oms.api.infrastructure.config

interface FeatureConfig {
    val name: String
    fun isEnabled(): Boolean
    fun enable()
    fun disable()
}