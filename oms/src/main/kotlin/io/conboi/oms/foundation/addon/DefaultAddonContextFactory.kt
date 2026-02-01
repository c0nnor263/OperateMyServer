package io.conboi.oms.foundation.addon

import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.addon.AddonContextSpec
import io.conboi.oms.foundation.manager.DefaultFeatureManager
import io.conboi.oms.infrastructure.file.DefaultAddonPaths

object DefaultAddonContextFactory : AddonContextFactory {
    override fun create(spec: AddonContextSpec): AddonContext {
        val addonId = spec.id
        val paths = spec.pathsFactory?.invoke()
            ?: DefaultAddonPaths(addonId)

        val featureManager = spec.featureManagerFactory?.invoke()
            ?: DefaultFeatureManager(addonId)

        return AddonContextImpl(
            id = addonId,
            paths = paths,
            featureManager = featureManager,
        )
    }
}

