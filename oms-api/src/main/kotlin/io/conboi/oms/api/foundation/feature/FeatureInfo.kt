package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.foundation.info.OmsInfo
import io.conboi.oms.api.infrastructure.config.FeatureConfigInfo

/**
 * Information about a feature, used for registration and prioritization.
 *
 * @property id The unique identifier of the feature
 * @property priority The priority of the feature
 * @property configInfo Configuration information for the feature
 */
data class FeatureInfo(
    override val data: Map<String, Any?> = emptyMap(),
    val id: String = "",
    val priority: Priority = Priority.NONE,
    val commands: List<OMSCommandEntry> = emptyList(),
    val configInfo: FeatureConfigInfo? = null
) : OmsInfo
