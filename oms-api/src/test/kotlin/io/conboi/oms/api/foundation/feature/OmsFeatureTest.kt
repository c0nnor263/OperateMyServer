package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.infrastructure.config.FeatureConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify

class OmsFeatureTest : FunSpec({

    val mockConfig: FeatureConfig = mockk(relaxed = true)
    lateinit var feature: TestFeature

    beforeEach {
        feature = TestFeature()
    }

    afterEach {
        clearAllMocks()
    }

    context("config") {
        test("should throw when accessing config before initialization") {
            val ex = shouldThrow<IllegalStateException> {
                feature.config
            }
            ex.message shouldBe "Feature config is not initialized yet."
        }

        test("should return config after initialization") {
            feature.onOmsRegisterConfig(mockConfig)
            feature.config shouldBe mockConfig
        }
    }

    context("onOmsRegisterConfig") {
        test("should cast and store config") {
            feature.onOmsRegisterConfig(mockConfig)
            feature.config shouldBe mockConfig
        }
    }

    context("isEnabled") {
        test("should return true when config is enabled") {
            every { mockConfig.isEnabled() } returns true
            feature.onOmsRegisterConfig(mockConfig)
            feature.isEnabled() shouldBe true
        }

        test("should return false when config is disabled") {
            every { mockConfig.isEnabled() } returns false
            feature.onOmsRegisterConfig(mockConfig)
            feature.isEnabled() shouldBe false
        }
    }

    context("enable") {
        test("should call enable on config") {
            feature.onOmsRegisterConfig(mockConfig)
            feature.enable()
            verify { mockConfig.enable() }
        }
    }

    context("disable") {
        test("should call disable on config") {
            feature.onOmsRegisterConfig(mockConfig)
            feature.disable()
            verify { mockConfig.disable() }
        }
    }

    context("getFeatureCommands") {
        test("should return empty list by default") {
            feature.getFeatureCommands().shouldBeEmpty()
        }
    }

    context("markConfigUpdated") {
        test("should set isConfigurationUpdated to true") {
            feature.isConfigurationUpdated.shouldBeFalse()
            feature.flagConfigAsDirty()
            feature.isConfigurationUpdated.shouldBeTrue()
        }
    }

    context("onConfigUpdated") {
        test("should reset isConfigurationUpdated to false") {
            val tickEvent = mockk<OMSLifecycle.TickingEvent>()
            feature.flagConfigAsDirty()
            feature.isConfigurationUpdated.shouldBeTrue()

            feature.onConfigUpdated(tickEvent)
            feature.isConfigurationUpdated.shouldBeFalse()
        }
    }

    context("onOmsTick") {
        test("should call onConfigUpdated and reset configUpdated flag") {
            val tickEvent = mockk<OMSLifecycle.TickingEvent>()
            feature.flagConfigAsDirty()
            feature.isConfigurationUpdated.shouldBeTrue()

            feature.onOmsTick(tickEvent)
            feature.isConfigurationUpdated.shouldBeFalse()
        }

        test("should call watchConfig on each tick") {
            val tickEvent = mockk<OMSLifecycle.TickingEvent>()
            val spyFeature = spyk(TestFeature(), recordPrivateCalls = true)

            spyFeature.flagConfigAsDirty()
            spyFeature.onOmsTick(tickEvent)

            verify { spyFeature["watchConfig"]() }
        }

        test("should NOT call onConfigUpdated if config is not dirty") {
            val tickEvent = mockk<OMSLifecycle.TickingEvent>()
            val spyFeature = spyk(TestFeature(), recordPrivateCalls = true)

            spyFeature.onOmsTick(tickEvent)

            verify(exactly = 1) { spyFeature["watchConfig"]() }
            verify(exactly = 0) { spyFeature.onConfigUpdated(any()) }
        }

    }

    context("onEnabled/onDisabled") {
        test("onEnabled should flag config as dirty") {
            feature.isConfigurationUpdated.shouldBeFalse()
            feature.onEnabled()
            feature.isConfigurationUpdated.shouldBeTrue()
        }

        test("onDisabled should flag config as dirty") {
            feature.isConfigurationUpdated.shouldBeFalse()
            feature.onDisabled()
            feature.isConfigurationUpdated.shouldBeTrue()
        }
    }

    context("onOmsStarted") {
        test("should do nothing by default") {
            val event = mockk<OMSLifecycle.StartingEvent>()
            feature.onOmsStarted(event)
        }
    }

    context("onOmsStopping") {
        test("should do nothing by default") {
            val event = mockk<OMSLifecycle.StoppingEvent>()
            feature.onOmsStopping(event)
        }
    }
})

private class TestFeature : OmsFeature<FeatureConfig>() {
    override val info: FeatureInfo = FeatureInfo("testmod", FeatureInfo.Priority.NONE)
}
