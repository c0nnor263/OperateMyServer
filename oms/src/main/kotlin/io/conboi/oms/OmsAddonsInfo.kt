package io.conboi.oms

import io.conboi.oms.api.foundation.info.OmsInfo
import io.conboi.oms.foundation.addon.OmsAddonInfo

data class OmsAddonsInfo(
    override val data: Map<String, Any> = emptyMap(),
    val addonsInfo: List<OmsAddonInfo> = emptyList()
) : OmsInfo