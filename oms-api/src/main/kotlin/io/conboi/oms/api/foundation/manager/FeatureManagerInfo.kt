package io.conboi.oms.api.foundation.manager

import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.info.OmsInfo

data class FeatureManagerInfo(
    override val data: Map<String, Any> = emptyMap(),
    val id: String,
    val modId: String,
    val name: String,
    val featuresInfo: List<FeatureInfo>
) : OmsInfo
