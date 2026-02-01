package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.TestFeature
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.api.infrastructure.config.FeatureConfig
import io.conboi.oms.api.infrastructure.config.FeatureConfigInfo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify

class OmsFeatureTest : ShouldSpec({

    lateinit var sut: TestFeature
    val mockConfig: FeatureConfig = mockk(relaxed = true)
    val mockProvider: ConfigProvider<FeatureConfig> = mockk()
    val mockAddonContext = mockk<AddonContext>(relaxed = true)

    beforeEach {
        every { mockProvider.get() } returns mockConfig

        sut = TestFeature(mockProvider)
    }

    context("config") {

        should("throw when accessing config before initialization") {
            val ex = shouldThrow<IllegalStateException> { sut.config }
            ex.message shouldBe "Feature config is not initialized yet."
        }

        should("return config after initialization") {
            sut.onOmsRegisterConfig()
            sut.config shouldBe mockConfig
        }
    }

    context("onOmsRegisterConfig") {

        should("store config from provider") {
            sut.onOmsRegisterConfig()
            verify { mockProvider.get() }
            sut.config shouldBe mockConfig
        }
    }

    context("isEnabled") {

        should("return true when config is enabled") {
            every { mockConfig.isEnabled() } returns true
            sut.onOmsRegisterConfig()
            sut.isEnabled() shouldBe true
        }

        should("return false when config is disabled") {
            every { mockConfig.isEnabled() } returns false
            sut.onOmsRegisterConfig()
            sut.isEnabled() shouldBe false
        }
    }

    context("enable") {

        should("call enable on config and mark dirty") {
            sut.onOmsRegisterConfig()
            sut.enable()
            verify { mockConfig.enable() }
            sut.isConfigDirty.shouldBeTrue()
        }
    }

    context("disable") {

        should("call disable on config and mark dirty") {
            sut.onOmsRegisterConfig()
            sut.disable()
            verify { mockConfig.disable() }
            sut.isConfigDirty.shouldBeTrue()
        }
    }

    context("additionalCommands") {
        should("be empty by default") {
            sut.additionalCommands.shouldBeEmpty()
        }
    }

    context("markConfigUpdated") {
        should("set isConfigDirty to true") {
            sut.isConfigDirty.shouldBeFalse()
            sut.markConfigAsDirty()
            sut.isConfigDirty.shouldBeTrue()
        }
    }

    context("onConfigUpdated") {

        should("reset isConfigDirty after tick") {
            val tick = mockk<OMSLifecycle.TickingEvent>()
            sut.onOmsRegisterConfig()
            sut.markConfigAsDirty()

            sut.onOmsTick(tick, mockAddonContext)

            sut.isConfigDirty.shouldBeFalse()
        }
    }

    context("onOmsTick") {

        should("call onConfigUpdated when dirty") {
            val tick = mockk<OMSLifecycle.TickingEvent>()
            val spy = spyk(TestFeature(mockProvider), recordPrivateCalls = true)

            every { mockProvider.get() } returns mockConfig
            spy.onOmsRegisterConfig()
            spy.markConfigAsDirty()

            spy.onOmsTick(tick, mockAddonContext)

            verify { spy.onConfigUpdated(tick) }
            spy.isConfigDirty.shouldBeFalse()
        }

        should("call watchConfig every tick") {
            val tick = mockk<OMSLifecycle.TickingEvent>()
            val spy = spyk(TestFeature(mockProvider), recordPrivateCalls = true)

            every { mockProvider.get() } returns mockConfig
            spy.onOmsRegisterConfig()

            spy.onOmsTick(tick, mockAddonContext)

            verify { spy["watchConfig"]() }
        }

        should("not call onConfigUpdated when clean") {
            val tick = mockk<OMSLifecycle.TickingEvent>()
            val spy = spyk(TestFeature(mockProvider), recordPrivateCalls = true)

            every { mockProvider.get() } returns mockConfig
            spy.onOmsRegisterConfig()

            spy.onOmsTick(tick, mockAddonContext)

            verify(exactly = 0) { spy.onConfigUpdated(any()) }
        }
    }

    context("info") {

        should("return info with id and configInfo") {
            val mockConfigInfo = mockk<FeatureConfigInfo>()
            every { mockConfig.name } returns "featureX"
            every { mockConfig.info() } returns mockConfigInfo

            sut.onOmsRegisterConfig()

            val info = sut.info()
            info.id shouldBe "featureX"
            info.configInfo shouldBe mockConfigInfo
        }

        should("return empty id when config is not initialized") {
            val info = sut.info()
            info.id shouldBe ""
            info.configInfo shouldBe null
        }
    }

    context("createEventListeners") {
        should("return empty list by default") {
            val listeners = sut.createEventListeners()
            listeners.shouldBeEmpty()
        }
    }
})