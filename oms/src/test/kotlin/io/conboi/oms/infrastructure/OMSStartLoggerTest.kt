package io.conboi.oms.infrastructure

import io.conboi.oms.OmsAddons
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.foundation.manager.FeatureManagerInfo
import io.conboi.oms.api.infrastructure.config.FeatureConfigInfo
import io.conboi.oms.common.infrastructure.config.CValue
import io.conboi.oms.common.infrastructure.config.ConfigBase
import io.conboi.oms.common.infrastructure.config.ConfigGroup
import io.conboi.oms.common.infrastructure.log.LOG
import io.conboi.oms.foundation.addon.OmsAddonInfo
import io.conboi.oms.infrastructure.config.CCommon
import io.conboi.oms.infrastructure.config.OMSConfigs
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify

class OMSStartLoggerTest : ShouldSpec({

    val mockCommon = mockk<CCommon>(relaxed = true)

    beforeSpec {
        mockkObject(LOG)
        mockkObject(OmsAddons)
        mockkObject(OMSConfigs)
    }

    beforeEach {
        every { LOG.info(any<String>()) } just Runs
        every { OMSConfigs.server.common } returns mockCommon
    }

    afterEach {
        clearAllMocks()
    }

    fun feature(
        id: String,
        data: Map<String, Any?> = emptyMap(),
        config: FeatureConfigInfo? = null
    ) = FeatureInfo(
        id = id,
        priority = Priority.COMMON,
        data = data,
        configInfo = config
    )

    fun manager(
        name: String,
        modId: String,
        data: Map<String, Any> = emptyMap(),
        features: List<FeatureInfo> = emptyList()
    ) = FeatureManagerInfo(
        id = name,
        addonId = modId,
        name = name,
        data = data,
        featuresInfo = features
    )

    fun addon(
        id: String,
        manager: FeatureManagerInfo
    ) = OmsAddonInfo(
        id = id,
        featureManagerInfo = manager
    )

    should("log OMS config with values, children and skip ConfigGroup") {
        val value = mockk<CValue<Any, *>>(relaxed = true)
        every { value.name } returns "enabled"

        val group = mockk<ConfigGroup>(relaxed = true)

        val childValue = mockk<CValue<Any, *>>(relaxed = true)
        every { childValue.name } returns "count"

        val childConfig = mockk<ConfigBase>(relaxed = true)
        every { childConfig.name } returns "child"
        every { childConfig.allValues } returns mutableListOf(childValue)
        every { childConfig.children } returns mutableListOf()

        every { mockCommon.name } returns "common"
        every { mockCommon.allValues } returns mutableListOf(group, value)
        every { mockCommon.children } returns mutableListOf(childConfig)

        every { OmsAddons.info().addonsInfo } returns emptyList()

        OmsStartLogger().showGreetings()

        verify { LOG.info(match<String> { it.contains("OMS Configuration:") }) }
        verify { LOG.info(match<String> { it.contains("enabled:") }) }
        verify { LOG.info(match<String> { it.contains("child:") }) }
        verify { LOG.info(match<String> { it.contains("count:") }) }
    }


    should("log addons managers and features") {
        val addonA = addon(
            "modA",
            manager(
                "ManagerA",
                "modA",
                data = mapOf("root" to 1),
                features = listOf(feature("FeatureA"))
            )
        )

        val addonB = addon(
            "modB",
            manager(
                "ManagerB",
                "modB",
                features = listOf(feature("FeatureB"))
            )
        )

        every { mockCommon.name } returns "common"
        every { mockCommon.allValues } returns mutableListOf()
        every { mockCommon.children } returns mutableListOf()

        every { OmsAddons.info().addonsInfo } returns listOf(addonB, addonA)

        OmsStartLogger().showGreetings()

        verify { LOG.info(match<String> { it.contains("• modA") }) }
        verify { LOG.info(match<String> { it.contains("• modB") }) }
        verify { LOG.info(match<String> { it.contains("ManagerA") }) }
        verify { LOG.info(match<String> { it.contains("ManagerB") }) }
        verify { LOG.info(match<String> { it.contains("FeatureA") }) }
        verify { LOG.info(match<String> { it.contains("FeatureB") }) }
    }

    should("log internal and config blocks") {
        val feature = feature(
            id = "Feature",
            data = mapOf("a" to 1, "b" to listOf(1, 2)),
            config = FeatureConfigInfo(
                name = "cfg",
                isEnabled = true,
                data = mapOf("c" to "x")
            )
        )

        every { mockCommon.name } returns "common"
        every { mockCommon.allValues } returns mutableListOf()
        every { mockCommon.children } returns mutableListOf()

        every { OmsAddons.info().addonsInfo } returns listOf(
            addon("mod", manager("Manager", "mod", features = listOf(feature)))
        )

        OmsStartLogger().showGreetings()

        verify { LOG.info(match<String> { it.contains("internal:") }) }
        verify { LOG.info(match<String> { it.contains("config:") }) }
        verify { LOG.info(match<String> { it.contains("a: 1") }) }
        verify { LOG.info(match<String> { it.contains("b: [1, 2]") }) }
        verify { LOG.info(match<String> { it.contains("c: x") }) }
    }

    should("skip empty internal and config blocks") {
        val feature = feature("Empty")

        every { mockCommon.name } returns "common"
        every { mockCommon.allValues } returns mutableListOf()
        every { mockCommon.children } returns mutableListOf()

        every { OmsAddons.info().addonsInfo } returns listOf(
            addon("mod", manager("Manager", "mod", features = listOf(feature)))
        )

        OmsStartLogger().showGreetings()

        verify(exactly = 0) { LOG.info(match<String> { it.contains("internal:") }) }
        verify(exactly = 0) { LOG.info(match<String> { it.contains("config:") }) }
    }

    should("log nested maps and primitive values") {
        val feature = feature(
            "Nested",
            data = mapOf(
                "map" to mapOf("x" to 1),
                "text" to "hello"
            )
        )

        every { mockCommon.name } returns "common"
        every { mockCommon.allValues } returns mutableListOf()
        every { mockCommon.children } returns mutableListOf()

        every { OmsAddons.info().addonsInfo } returns listOf(
            addon("mod", manager("Manager", "mod", features = listOf(feature)))
        )

        OmsStartLogger().showGreetings()

        verify { LOG.info(match<String> { it.contains("map:") }) }
        verify { LOG.info(match<String> { it.contains("x: 1") }) }
        verify { LOG.info(match<String> { it.contains("text: hello") }) }
    }

    should("build correct prefixes") {
        val logger = OmsStartLogger()

        val p1 = logger.run {
            val m = OmsStartLogger::class.java.getDeclaredMethod(
                "prefix",
                List::class.java,
                Boolean::class.java
            )
            m.isAccessible = true
            m.invoke(this, listOf(true, false, true), false) as String
        }

        val p2 = logger.run {
            val m = OmsStartLogger::class.java.getDeclaredMethod(
                "prefix",
                List::class.java,
                Boolean::class.java
            )
            m.isAccessible = true
            m.invoke(this, listOf(false, false), true) as String
        }

        p1 shouldBe "│     │  ├─ "
        p2 shouldBe "      └─ "
    }
})
