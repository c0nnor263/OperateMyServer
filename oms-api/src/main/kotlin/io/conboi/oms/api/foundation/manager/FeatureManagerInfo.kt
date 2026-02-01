package io.conboi.oms.api.foundation.manager

import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.info.OmsInfo

/**
 * Information snapshot describing a feature manager and its managed features.
 *
 * This data class represents immutable, structured diagnostic information
 * about a feature manager, including its identity, ownership, and the
 * current set of registered features.
 *
 * It is primarily intended for status reporting, diagnostics, and
 * external inspection.
 *
 * @property data additional, manager-specific metadata
 * @property id the unique identifier of the feature manager
 * @property addonId the identifier of the addon that owns this manager
 * @property name the logical name of the feature manager
 * @property featuresInfo information snapshots of managed features
 */
data class FeatureManagerInfo(
    override val data: Map<String, Any> = emptyMap(),
    val id: String,
    val addonId: String,
    val name: String,
    val featuresInfo: List<FeatureInfo>
) : OmsInfo
