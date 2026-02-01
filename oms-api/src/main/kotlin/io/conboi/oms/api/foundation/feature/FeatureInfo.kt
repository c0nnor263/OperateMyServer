package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.foundation.info.OmsInfo
import io.conboi.oms.api.infrastructure.config.FeatureConfigInfo

/**
 * Information snapshot describing an OMS feature.
 *
 * This data class represents immutable metadata used for
 * feature registration, prioritization, and integration
 * with the OMS command and configuration systems.
 *
 * It is not a live view of the feature state, but a
 * descriptive snapshot intended for discovery, diagnostics,
 * and lifecycle coordination.
 *
 * @property data additional, feature-specific metadata
 * @property id the unique identifier of the feature
 * @property priority the priority used for lifecycle ordering
 * @property additionalCommands command entries exposed by the feature
 * @property configInfo configuration metadata associated with the feature
 */
data class FeatureInfo(
    override val data: Map<String, Any?> = emptyMap(),
    val id: String = "",
    val priority: Priority = Priority.NONE,
    val additionalCommands: List<OMSCommandEntry> = emptyList(),
    val configInfo: FeatureConfigInfo? = null
) : OmsInfo
