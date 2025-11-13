package io.conboi.oms.api.foundation.file

import io.conboi.oms.api.extension.ensure
import io.conboi.oms.api.foundation.feature.FeatureInfo
import java.nio.file.Path

abstract class AddonPaths(private val modId: String) {
    lateinit var omsRoot: Path
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

    fun onInitializeOmsRoot(omsRootPath: Path) {
        omsRoot = omsRootPath
    }
}