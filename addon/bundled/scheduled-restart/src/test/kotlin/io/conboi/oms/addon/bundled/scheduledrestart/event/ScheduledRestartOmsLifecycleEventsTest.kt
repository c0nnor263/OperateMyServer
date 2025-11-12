package io.conboi.oms.addon.bundled.scheduledrestart.event

import io.conboi.oms.addon.bundled.scheduledrestart.ScheduledRestartFeature
import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.core.infrastructure.config.CServer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify

class ScheduledRestartOmsLifecycleEventsTest : FunSpec({

    beforeTest {
        mockkObject(OMSFeatureManagers)
        mockkObject(CServer.features)
    }

    afterTest {
        unmockkAll()
    }

    test("should register ScheduledRestartFeature on RegisterEvent") {
        val slot = slot<ScheduledRestartFeature>()
        every { OMSFeatureManagers.oms.register(capture(slot)) } just Runs

        ScheduledRestartOmsLifecycleEvents.onRegisterFeatureEvent(OMSLifecycle.Feature.RegisterEvent())

        slot.isCaptured shouldBe true
        slot.captured shouldBe instanceOf<ScheduledRestartFeature>()
    }

    test("should register config on RegisterConfigEvent") {
        val config = mockk<CScheduledRestartFeature>()
        val feature = mockk<ScheduledRestartFeature>(relaxed = true)

        every { CServer.features.getFeatureConfig<CScheduledRestartFeature>(any()) } returns config
        every { OMSFeatureManagers.oms.getFeatureById<ScheduledRestartFeature>(any()) } returns feature

        ScheduledRestartOmsLifecycleEvents.onRegisterFeatureConfigEvent(OMSLifecycle.Feature.RegisterConfigEvent())

        verify { feature.onOmsRegisterConfig(config) }
    }

    test("should not throw if feature not found on RegisterConfigEvent") {
        val config = mockk<CScheduledRestartFeature>()

        every { CServer.features.getFeatureConfig<CScheduledRestartFeature>(any()) } returns config
        every { OMSFeatureManagers.oms.getFeatureById<ScheduledRestartFeature>(any()) } returns null

        // no exception should be thrown
        ScheduledRestartOmsLifecycleEvents.onRegisterFeatureConfigEvent(OMSLifecycle.Feature.RegisterConfigEvent())
    }
})