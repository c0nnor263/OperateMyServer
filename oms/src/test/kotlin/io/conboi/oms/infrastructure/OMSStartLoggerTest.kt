package io.conboi.oms.infrastructure

import io.conboi.oms.api.OmsAddons
import io.conboi.oms.api.foundation.addon.OmsAddonInfo
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.foundation.manager.FeatureManagerInfo
import io.conboi.oms.api.infrastructure.config.FeatureConfigInfo
import io.conboi.oms.core.infrastructure.LOG
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.verify

class OMSStartLoggerTest : ShouldSpec({

    beforeSpec {
        mockkObject(LOG)
        mockkObject(OmsAddons)
    }

    beforeEach {
        every { LOG.info(any<String>()) } just Runs
    }

    afterEach {
        clearAllMocks()
    }

    fun config(enabled: Boolean, data: Map<String, Any>) =
        FeatureConfigInfo(
            name = "cfg",
            isEnabled = enabled,
            data = data
        )

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
        modId = modId,
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

    context("showGreetings") {

        should("log addons, managers and features") {
            val addonA1 = addon(
                id = "modA",
                manager = manager(
                    name = "ManagerOne",
                    modId = "modA",
                    data = mapOf("rootA" to 1),
                    features = listOf(feature("FeatureA"))
                )
            )

            val addonA2 = addon(
                id = "modA2",
                manager = manager(
                    name = "ManagerTwo",
                    modId = "modA2",
                    features = listOf(feature("FeatureB"))
                )
            )

            val addonB = addon(
                id = "modB",
                manager = manager(
                    name = "ManagerThree",
                    modId = "modB",
                    features = listOf(feature("FeatureC"))
                )
            )

            every { OmsAddons.info().addonsInfo } returns listOf(
                addonA2,
                addonB,
                addonA1
            )

            OMSStartLogger().showGreetings()

            verify { LOG.info(match<String> { it.contains("Operate My Server initialized successfully!") }) }
            verify { LOG.info(match<String> { it.contains("Loaded Addons and Features:") }) }

            verify { LOG.info(match<String> { it.contains("• modA") }) }
            verify { LOG.info(match<String> { it.contains("• modA2") }) }
            verify { LOG.info(match<String> { it.contains("• modB") }) }

            verify { LOG.info(match<String> { it.contains("ManagerOne") }) }
            verify { LOG.info(match<String> { it.contains("ManagerTwo") }) }
            verify { LOG.info(match<String> { it.contains("ManagerThree") }) }
        }

        should("log manager data when present") {
            val manager = manager(
                name = "ManagerData",
                modId = "modX",
                data = mapOf("value" to 42)
            )

            every { OmsAddons.info().addonsInfo } returns listOf(
                addon("modX", manager)
            )

            OMSStartLogger().showGreetings()

            verify { LOG.info(match<String> { it.contains("value: 42") }) }
        }
    }

    context("feature logging") {

        should("log internal and config blocks") {
            val internal = mapOf(
                "internalKey" to 1,
                "internalList" to listOf("a", "b")
            )
            val configData = mapOf(
                "configKey" to "xyz"
            )

            val feature = feature(
                id = "FeatureWithBoth",
                data = internal,
                config = config(enabled = true, data = configData)
            )

            val addon = addon(
                id = "mod",
                manager = manager(
                    name = "Manager",
                    modId = "mod",
                    features = listOf(feature)
                )
            )

            every { OmsAddons.info().addonsInfo } returns listOf(addon)

            OMSStartLogger().showGreetings()

            verify { LOG.info(match<String> { it.contains("FeatureWithBoth") }) }
            verify { LOG.info(match<String> { it.contains("internal:") }) }
            verify { LOG.info(match<String> { it.contains("config:") }) }
            verify { LOG.info(match<String> { it.contains("internalKey: 1") }) }
            verify { LOG.info(match<String> { it.contains("internalList: [a, b]") }) }
            verify { LOG.info(match<String> { it.contains("configKey: xyz") }) }
        }

        should("log only internal when config is null") {
            val feature = feature(
                id = "InternalOnly",
                data = mapOf("onlyInternal" to 10),
                config = null
            )

            val addon = addon(
                id = "mod",
                manager = manager(
                    name = "Manager",
                    modId = "mod",
                    features = listOf(feature)
                )
            )

            every { OmsAddons.info().addonsInfo } returns listOf(addon)

            OMSStartLogger().showGreetings()

            verify { LOG.info(match<String> { it.contains("InternalOnly") }) }
            verify { LOG.info(match<String> { it.contains("internal:") }) }
            verify { LOG.info(match<String> { it.contains("onlyInternal: 10") }) }
            verify(exactly = 0) { LOG.info(match<String> { it.contains("config:") }) }
        }

        should("log only config when internal is empty") {
            val feature = feature(
                id = "ConfigOnly",
                data = emptyMap(),
                config = config(enabled = true, data = mapOf("onlyConfig" to 99))
            )

            val addon = addon(
                id = "mod",
                manager = manager(
                    name = "Manager",
                    modId = "mod",
                    features = listOf(feature)
                )
            )

            every { OmsAddons.info().addonsInfo } returns listOf(addon)

            OMSStartLogger().showGreetings()

            verify { LOG.info(match<String> { it.contains("ConfigOnly") }) }
            verify { LOG.info(match<String> { it.contains("onlyConfig: 99") }) }
            verify(exactly = 0) { LOG.info(match<String> { it.contains("internal:") }) }
        }

        should("skip internal and config blocks when both empty") {
            val feature = feature(
                id = "EmptyFeature",
                data = emptyMap(),
                config = null
            )

            val addon = addon(
                id = "mod",
                manager = manager(
                    name = "Manager",
                    modId = "mod",
                    features = listOf(feature)
                )
            )

            every { OmsAddons.info().addonsInfo } returns listOf(addon)

            OMSStartLogger().showGreetings()

            verify { LOG.info(match<String> { it.contains("EmptyFeature") }) }
            verify(exactly = 0) { LOG.info(match<String> { it.contains("internal:") }) }
            verify(exactly = 0) { LOG.info(match<String> { it.contains("config:") }) }
        }
    }

    context("data entry") {

        should("log nested maps recursively") {
            val nested = mapOf(
                "nested" to mapOf(
                    "firstKey" to 1,
                    "secondKey" to 2
                )
            )

            val feature = feature(
                id = "NestedFeature",
                data = nested
            )

            val addon = addon(
                id = "mod",
                manager = manager(
                    name = "Manager",
                    modId = "mod",
                    features = listOf(feature)
                )
            )

            every { OmsAddons.info().addonsInfo } returns listOf(addon)

            OMSStartLogger().showGreetings()

            verify { LOG.info(match<String> { it.contains("nested:") }) }
            verify { LOG.info(match<String> { it.contains("firstKey: 1") }) }
            verify { LOG.info(match<String> { it.contains("secondKey: 2") }) }
        }

        should("log iterable values as lists") {
            val feature = feature(
                id = "IterableFeature",
                data = mapOf("numbers" to listOf(1, 2, 3))
            )

            val addon = addon(
                id = "mod",
                manager = manager(
                    name = "Manager",
                    modId = "mod",
                    features = listOf(feature)
                )
            )

            every { OmsAddons.info().addonsInfo } returns listOf(addon)

            OMSStartLogger().showGreetings()

            verify { LOG.info(match<String> { it.contains("numbers: [1, 2, 3]") }) }
        }
    }

    context("prefix") {

        should("build prefix for non-last item") {
            val logger = OMSStartLogger()
            val prefix = logger.run {
                val method = OMSStartLogger::class.java.getDeclaredMethod(
                    "prefix",
                    List::class.java,
                    Boolean::class.java
                )
                method.isAccessible = true
                method.invoke(this, listOf(true, false, true), false) as String
            }
            prefix shouldBe "│     │  ├─ "
        }

        should("build prefix for last item") {
            val logger = OMSStartLogger()
            val prefix = logger.run {
                val method = OMSStartLogger::class.java.getDeclaredMethod(
                    "prefix",
                    List::class.java,
                    Boolean::class.java
                )
                method.isAccessible = true
                method.invoke(this, listOf(false, false), true) as String
            }
            prefix shouldBe "      └─ "
        }
    }
})
