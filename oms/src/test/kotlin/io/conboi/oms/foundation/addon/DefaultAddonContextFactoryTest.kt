package io.conboi.oms.foundation.addon

import io.conboi.oms.api.foundation.addon.AddonContextSpec
import io.conboi.oms.api.foundation.manager.FeatureManager
import io.conboi.oms.api.infrastructure.file.AddonPaths
import io.conboi.oms.foundation.manager.DefaultFeatureManager
import io.conboi.oms.infrastructure.file.DefaultAddonPaths
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk

class DefaultAddonContextFactoryTest : ShouldSpec({

    should("create context with default paths and feature manager") {
        val spec = AddonContextSpec(id = "test")

        val context = DefaultAddonContextFactory.create(spec)

        context.id shouldBe "test"
        context.paths.shouldBeInstanceOf<DefaultAddonPaths>()
        context.featureManager.shouldBeInstanceOf<DefaultFeatureManager>()
    }

    should("use custom pathsFactory when provided") {
        val customPaths = mockk<AddonPaths>()

        val spec = AddonContextSpec(
            id = "test",
            pathsFactory = { customPaths }
        )

        val context = DefaultAddonContextFactory.create(spec)

        context.paths shouldBe customPaths
        context.featureManager.shouldBeInstanceOf<DefaultFeatureManager>()
    }

    should("use custom featureManagerFactory when provided") {
        val customManager = mockk<FeatureManager>()

        val spec = AddonContextSpec(
            id = "test",
            featureManagerFactory = { customManager }
        )

        val context = DefaultAddonContextFactory.create(spec)

        context.featureManager shouldBe customManager
        context.paths.shouldBeInstanceOf<DefaultAddonPaths>()
    }

    should("use both custom factories when provided") {
        val customPaths = mockk<AddonPaths>()
        val customManager = mockk<FeatureManager>()

        val spec = AddonContextSpec(
            id = "test",
            pathsFactory = { customPaths },
            featureManagerFactory = { customManager }
        )

        val context = DefaultAddonContextFactory.create(spec)

        context.id shouldBe "test"
        context.paths shouldBe customPaths
        context.featureManager shouldBe customManager
    }
})

