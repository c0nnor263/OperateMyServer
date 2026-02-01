package io.conboi.oms.api.infrastructure.config

/**
 * Provides a feature configuration instance.
 *
 * A [ConfigProvider] is responsible for supplying a fully initialized
 * [FeatureConfig] to an OMS feature during the configuration registration phase.
 *
 * This abstraction decouples feature logic from the underlying
 * configuration storage and loading mechanism.
 *
 * @param T the type of feature configuration provided
 */
fun interface ConfigProvider<T : FeatureConfig> {

    /**
     * Returns the feature configuration instance.
     *
     * This method is typically invoked once during the OMS
     * configuration registration phase.
     *
     * Implementations should return a stable configuration
     * instance for the lifetime of the feature.
     *
     * @return the feature configuration
     */
    fun get(): T
}
