package io.conboi.oms.api.infrastructure.config

fun interface ConfigProvider<T : FeatureConfig> {
    fun get(): T
}
