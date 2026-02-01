package io.conboi.oms.api.foundation.addon

import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.manager.FeatureManager
import io.conboi.oms.api.infrastructure.file.AddonPaths
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class OmsAddonTest : ShouldSpec({

    lateinit var sut: OmsAddon
    val mockFeatureManager: FeatureManager = mockk(relaxed = true)

    val mockFeature1: OmsFeature<*> = mockk(relaxed = true)
    val mockFeature2: OmsFeature<*> = mockk(relaxed = true)
    val mockAddonContext = mockk<AddonContext>(relaxed = true)

    beforeEach {
        every { mockAddonContext.featureManager } returns mockFeatureManager
        sut = object : OmsAddon("testaddon") {
            override fun onRegisterFeatures(context: AddonContext) {
                super.onRegisterFeatures(context)
                context.featureManager.register(mockFeature1)
                context.featureManager.register(mockFeature2)
            }
        }
    }

    afterEach {
        clearAllMocks()
    }

    context("constructor") {

        should("accept valid id") {
            val addon = object : OmsAddon("valid_id-1") {}
            addon.id shouldBe "valid_id-1"
        }
    }

    context("onRegisterFeatures") {
        should("register provided features in featureManager") {
            sut.onRegisterFeatures(mockAddonContext)

            verify { mockFeatureManager.register(mockFeature1) }
            verify { mockFeatureManager.register(mockFeature2) }
        }
    }

    context("configureContext") {
        should("return unmodified AddonContextSpec by default") {
            val originalSpec = AddonContextSpec(
                id = "testaddon",
                pathsFactory = null,
                featureManagerFactory = null
            )
            val modifiedSpec = sut.configureContext(originalSpec)
            modifiedSpec shouldBe originalSpec
        }

        should("allow overriding AddonContextSpec properties") {
            val customPathsFactory = { mockk<AddonPaths>() }
            val customFeatureManagerFactory = { mockk<FeatureManager>() }

            sut = object : OmsAddon("testaddon") {
                override fun configureContext(spec: AddonContextSpec): AddonContextSpec {
                    return spec.copy(
                        pathsFactory = customPathsFactory,
                        featureManagerFactory = customFeatureManagerFactory
                    )
                }
            }

            val originalSpec = AddonContextSpec(
                id = "custom_addon",
                pathsFactory = null,
                featureManagerFactory = null
            )

            val modifiedSpec = sut.configureContext(originalSpec)
            modifiedSpec.id shouldBe "custom_addon"
            modifiedSpec.pathsFactory shouldBe customPathsFactory
            modifiedSpec.featureManagerFactory shouldBe customFeatureManagerFactory
        }
    }
})

