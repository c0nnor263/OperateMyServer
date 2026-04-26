package io.conboi.oms.infrastructure

import io.conboi.oms.OmsAddons
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.manager.FeatureManagerInfo
import io.conboi.oms.api.infrastructure.config.FeatureConfigInfo
import io.conboi.oms.common.infrastructure.config.ConfigBase
import io.conboi.oms.common.infrastructure.config.ConfigGroup
import io.conboi.oms.common.infrastructure.log.LOG
import io.conboi.oms.foundation.addon.OmsAddonInfo
import io.conboi.oms.infrastructure.config.CCommon
import io.conboi.oms.infrastructure.config.OMSConfigs

// TODO: Think about reformatting the log output to be informative
// TODO FUTURE: Extract InfoRenderer when commands (/oms info, status) are implemented
class OmsStartLogger {

    fun showGreetings() {
        LOG.info("============================================")
        LOG.info("Operate My Server initialized successfully!")
        LOG.info("--------------------------------------------")
        logOmsConfig(OMSConfigs.server.common)
        LOG.info("--------------------------------------------")
        LOG.info("Loaded Addons and Features:")
        LOG.info("--------------------------------------------")

        OmsAddons.info()
            .addonsInfo
            .sortedBy { it.id }
            .forEach(::logAddon)

        LOG.info("--------------------------------------------")
        LOG.info("============================================")
    }

    private fun logOmsConfig(config: CCommon) {
        LOG.info("OMS Configuration:")
        logDataBlock(
            name = config.name,
            map = configToMap(config),
            isLast = true,
            levels = emptyList()
        )
    }

    private fun configToMap(config: ConfigBase): Map<String, Any?> {
        val result = LinkedHashMap<String, Any?>()

        for (cv in config.allValues) {
            if (cv is ConfigGroup) continue

            val v = cv.get() ?: continue
            result[cv.name] = config.normalizeValue(v) ?: continue
        }

        for (child in config.children) {
            if (child == null) continue
            val childMap = configToMap(child)
            if (childMap.isNotEmpty()) {
                result[child.name] = childMap
            }
        }

        return result
    }


    private fun logAddon(addon: OmsAddonInfo) {
        LOG.info("• ${addon.id}")
        val levels = mutableListOf<Boolean>()
        // TODO FUTURE: Handle multiple managers
        logManager(addon.featureManagerInfo, true, levels)
    }

    private fun logManager(
        manager: FeatureManagerInfo,
        isLast: Boolean,
        levels: List<Boolean>
    ) {
        LOG.info(prefix(levels, isLast) + manager.id)

        val next = levels + !isLast

        logDataBlockIfNotEmpty("data", manager.data, next)

        manager.featuresInfo
            .sortedByDescending { it.priority }
            .forEachIndexed { index, feature ->
                val lastFeature = index == manager.featuresInfo.size - 1
                logFeature(feature, lastFeature, next)
            }
    }

    private fun logFeature(
        feature: FeatureInfo,
        isLast: Boolean,
        levels: List<Boolean>
    ) {
        LOG.info(prefix(levels, isLast) + feature.id)

        val next = levels + !isLast

        logFeatureFull(feature, feature.configInfo, next)
    }

    private fun logFeatureFull(
        feature: FeatureInfo,
        config: FeatureConfigInfo?,
        levels: List<Boolean>
    ) {
        val internal = feature.data
        val configData = config?.data ?: emptyMap()
        val blocks = buildList {
            if (internal.isNotEmpty()) add("internal")
            if (configData.isNotEmpty()) add("config")
        }

        var index = 0
        val lastIndex = blocks.size - 1

        if (internal.isNotEmpty()) {
            logDataBlock(
                name = "internal",
                map = internal,
                isLast = index == lastIndex,
                levels = levels
            )
            index++
        }

        if (configData.isNotEmpty()) {
            logDataBlock(
                name = "config",
                map = configData,
                isLast = index == lastIndex,
                levels = levels
            )
        }
    }

    private fun logDataBlockIfNotEmpty(
        name: String,
        data: Map<String, Any>,
        levels: List<Boolean>
    ) {
        if (data.isNotEmpty()) {
            logDataBlock(name, data, false, levels)
        }
    }

    private fun logDataBlock(
        name: String,
        map: Map<String, Any?>,
        isLast: Boolean,
        levels: List<Boolean>
    ) {
        LOG.info(prefix(levels, isLast) + "$name:")
        val next = levels + !isLast

        val entries = map.entries.toList()
        entries.forEachIndexed { index, entry ->
            val last = index == entries.size - 1
            logDataEntry(entry.key, entry.value, last, next)
        }
    }

    private fun logDataEntry(
        key: String,
        value: Any?,
        isLast: Boolean,
        levels: List<Boolean>
    ) {
        when (value) {
            is Map<*, *> -> {
                LOG.info(prefix(levels, isLast) + "$key:")
                val next = levels + !isLast
                val entries = value.entries.toList()
                entries.forEachIndexed { i, entry ->
                    val last = i == entries.size - 1
                    logDataEntry(entry.key.toString(), entry.value, last, next)
                }
            }

            is Iterable<*> -> {
                val listString = value.joinToString(prefix = "[", postfix = "]")
                LOG.info(prefix(levels, isLast) + "$key: $listString")
            }

            else -> {
                LOG.info(prefix(levels, isLast) + "$key: $value")
            }
        }
    }

    private fun prefix(levels: List<Boolean>, isLast: Boolean): String {
        val sb = StringBuilder()
        for (draw in levels) {
            sb.append(if (draw) "│  " else "   ")
        }
        sb.append(if (isLast) "└─ " else "├─ ")
        return sb.toString()
    }
}
