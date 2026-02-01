package io.conboi.oms.foundation.addon

import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.manager.FeatureManager
import io.conboi.oms.api.infrastructure.file.AddonPaths

data class AddonContextImpl(
    override val id: String,
    override val paths: AddonPaths,
    override val featureManager: FeatureManager,
) : AddonContext