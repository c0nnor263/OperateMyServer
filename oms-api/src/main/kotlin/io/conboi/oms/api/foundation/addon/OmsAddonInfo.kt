package io.conboi.oms.api.foundation.addon

import io.conboi.oms.api.foundation.info.OmsInfo
import io.conboi.oms.api.foundation.manager.FeatureManagerInfo

data class OmsAddonInfo(
    override val data: Map<String, Any> = emptyMap(),
    val id: String,
    val featureManagerInfo: FeatureManagerInfo
) : OmsInfo