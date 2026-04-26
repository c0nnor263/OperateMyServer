package io.conboi.oms.foundation.addon

import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.addon.AddonContextSpec

fun interface AddonContextFactory {
    fun create(spec: AddonContextSpec): AddonContext
}