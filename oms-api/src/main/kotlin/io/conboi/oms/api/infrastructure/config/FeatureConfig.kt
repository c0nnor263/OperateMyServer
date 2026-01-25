package io.conboi.oms.api.infrastructure.config

import io.conboi.oms.api.foundation.info.InfoProvider

/**
 * Defines the configuration contract for an OMS feature.
 *
 * A [FeatureConfig] represents the persistent, user-controlled
 * configuration state of a feature, including its enable/disable
 * status and optional configuration metadata.
 *
 * Implementations are typically backed by a concrete configuration
 * system (for example, TOML, JSON, or in-memory storage) and are
 * supplied to features via a [ConfigProvider].
 *
 * @see FeatureConfigInfo
 */
interface FeatureConfig : InfoProvider<FeatureConfigInfo> {

    /**
     * Logical name of the feature.
     *
     * This value is used to identify the feature in diagnostics,
     * configuration snapshots, and status reporting.
     */
    val name: String

    /**
     * Returns whether the feature is currently enabled.
     */
    fun isEnabled(): Boolean

    /**
     * Enables the feature.
     *
     * Implementations should update the underlying configuration state
     * accordingly.
     */
    fun enable()

    /**
     * Disables the feature.
     *
     * Implementations should update the underlying configuration state
     * accordingly.
     */
    fun disable()

    /**
     * Returns configuration-specific data as a key-value map.
     *
     * This data is intended for diagnostics, inspection, or
     * external reporting and must not be used to mutate runtime logic.
     *
     * @return a map containing configuration metadata
     */
    fun getConfigData(): Map<String, Any> = emptyMap()

    /**
     * Returns a snapshot of information describing this configuration.
     */
    override fun info(): FeatureConfigInfo = FeatureConfigInfo(
        name = name,
        isEnabled = isEnabled(),
        data = getConfigData()
    )
}