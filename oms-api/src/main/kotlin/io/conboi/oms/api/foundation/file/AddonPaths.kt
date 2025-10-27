package io.conboi.oms.api.foundation.file

import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.infrastructure.file.OMSRootPath
import io.conboi.oms.api.infrastructure.file.OMSRootPath.ensure
import java.nio.file.Path

abstract class AddonPaths(private val modId: String) {

    init {
        require(isValidId(modId)) { "Invalid modId: $modId" }
    }

    val omsRoot: Path get() = OMSRootPath.root
    val addonRoot: Path get() = omsRoot.ensure(modId)

    val common: Path get() = addonRoot.ensure("common")
    val logs: Path get() = addonRoot.ensure("logs")
    val cache: Path get() = addonRoot.ensure("cache")

    fun forFeature(info: FeatureInfo, type: AddonPathType): Path {
        return when (type) {
            AddonPathType.COMMON -> common.ensure(info.id)
            AddonPathType.LOGS -> logs.ensure(info.id)
            AddonPathType.CACHE -> cache.ensure(info.id)
        }
    }

    private fun isValidId(id: String): Boolean {
        return id.matches(Regex("[a-z0-9_\\-]+"))
    }
}