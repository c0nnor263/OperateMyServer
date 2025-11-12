package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.core.foundation.feature.WEFeatureManager
import io.conboi.oms.watchdogessentials.core.infrastructure.config.CServer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify

class EmptyServerRestartOmsLifecycleEventsTest : FunSpec({

    val mockWeFeatureManager = mockk<WEFeatureManager>()

    beforeEach {
        mockkObject(OMSFeatureManagers)
        mockkObject(CServer.features)

        every { OMSFeatureManagers.get<WEFeatureManager>(any()) } returns mockWeFeatureManager
    }

    afterEach {
        clearAllMocks()
    }

    test("should register EmptyServerRestartFeature on RegisterEvent") {
        val slot = slot<EmptyServerRestartFeature>()
        every { mockWeFeatureManager.register(capture(slot)) } just Runs

        EmptyServerRestartOmsLifecycleEvents.onRegisterFeaturesEvent(OMSLifecycle.Feature.RegisterEvent())

        slot.isCaptured shouldBe true
        slot.captured.shouldBeInstanceOf<EmptyServerRestartFeature>()
    }

    test("should register config on RegisterConfigEvent") {
        val config = mockk<CEmptyServerRestartFeature>()
        val feature = mockk<EmptyServerRestartFeature>(relaxed = true)

        every { CServer.features.getFeatureConfig<CEmptyServerRestartFeature>(any()) } returns config
        every { mockWeFeatureManager.getFeatureById<EmptyServerRestartFeature>(any()) } returns feature

        EmptyServerRestartOmsLifecycleEvents.onRegisterFeaturesConfigEvent(OMSLifecycle.Feature.RegisterConfigEvent())

        verify { feature.onOmsRegisterConfig(config) }
    }

    test("should not throw if feature not found on RegisterConfigEvent") {
        val config = mockk<CEmptyServerRestartFeature>()

        every { CServer.features.getFeatureConfig<CEmptyServerRestartFeature>(any()) } returns config
        every { mockWeFeatureManager.getFeatureById<EmptyServerRestartFeature>(any()) } returns null

        // should run without exception
        EmptyServerRestartOmsLifecycleEvents.onRegisterFeaturesConfigEvent(OMSLifecycle.Feature.RegisterConfigEvent())
    }
})