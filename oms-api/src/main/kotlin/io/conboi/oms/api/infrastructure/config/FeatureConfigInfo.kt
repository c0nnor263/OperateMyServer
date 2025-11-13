package io.conboi.oms.api.infrastructure.config

import io.conboi.oms.api.foundation.info.OmsInfo

data class FeatureConfigInfo(
    override val data: Map<String, Any> = emptyMap(),
    val name: String,
    val isEnabled: Boolean
) : OmsInfo