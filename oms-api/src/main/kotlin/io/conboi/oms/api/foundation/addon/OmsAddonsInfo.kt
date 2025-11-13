package io.conboi.oms.api.foundation.addon

import io.conboi.oms.api.foundation.info.OmsInfo

data class OmsAddonsInfo(
    override val data: Map<String, Any> = emptyMap(),
    val addonsInfo: List<OmsAddonInfo> = emptyList()
) : OmsInfo
