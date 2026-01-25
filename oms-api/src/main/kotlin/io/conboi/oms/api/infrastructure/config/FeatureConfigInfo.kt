package io.conboi.oms.api.infrastructure.config

import io.conboi.oms.api.foundation.info.OmsInfo

/**
 * Information snapshot describing a feature configuration.
 *
 * This data class represents immutable, structured metadata
 * about a feature's configuration state.
 *
 * It is intended for diagnostics, status reporting,
 * and external inspection.
 *
 * @property data additional, configuration-specific metadata
 * @property name the name of the feature associated with this configuration
 * @property isEnabled whether the feature is currently enabled
 */
data class FeatureConfigInfo(
    override val data: Map<String, Any> = emptyMap(),
    val name: String,
    val isEnabled: Boolean
) : OmsInfo