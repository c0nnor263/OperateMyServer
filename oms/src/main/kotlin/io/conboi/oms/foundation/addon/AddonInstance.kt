package io.conboi.oms.foundation.addon

import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.api.foundation.info.InfoProvider

data class AddonInstance(
    val addon: OmsAddon,
    val context: AddonContext
) : InfoProvider<OmsAddonInfo> {
    override fun info(): OmsAddonInfo = OmsAddonInfo(
        id = addon.id,
        featureManagerInfo = context.featureManager.info()
    )
}