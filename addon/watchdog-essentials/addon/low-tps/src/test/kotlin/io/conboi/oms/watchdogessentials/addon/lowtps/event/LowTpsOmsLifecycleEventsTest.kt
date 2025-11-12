package io.conboi.oms.watchdogessentials.addon.lowtps.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.watchdogessentials.addon.lowtps.LowTpsFeature
import io.conboi.oms.watchdogessentials.addon.lowtps.infrastructure.config.CLowTpsFeature
import io.conboi.oms.watchdogessentials.core.foundation.feature.WEFeatureManager
import io.conboi.oms.watchdogessentials.core.infrastructure.config.CServer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify

class LowTpsOmsLifecycleEventsTest : FunSpec({

    val mockWeFeatureManager: WEFeatureManager = mockk<WEFeatureManager>()
    beforeTest {
        mockkObject(OMSFeatureManagers)
        mockkObject(WEFeatureManager)
        mockkObject(CServer.features)

        every { OMSFeatureManagers.get<WEFeatureManager>(any()) } returns mockWeFeatureManager
    }

    afterTest {
        unmockkAll()
    }

    test("should register LowTpsFeature on RegisterEvent") {
        val slot = slot<LowTpsFeature>()
        every { mockWeFeatureManager.register(capture(slot)) } just Runs

        LowTpsOmsLifecycleEvents.onRegisterFeaturesEvent(OMSLifecycle.Feature.RegisterEvent())

        slot.isCaptured shouldBe true
        slot.captured.shouldBeInstanceOf<LowTpsFeature>()
    }

    test("should register config on RegisterConfigEvent") {
        val config = mockk<CLowTpsFeature>()
        val feature = mockk<LowTpsFeature>(relaxed = true)

        every { CServer.features.getFeatureConfig<CLowTpsFeature>(any()) } returns config
        every { mockWeFeatureManager.getFeatureById<LowTpsFeature>(any()) } returns feature

        LowTpsOmsLifecycleEvents.onRegisterFeaturesConfigEvent(OMSLifecycle.Feature.RegisterConfigEvent())

        verify { feature.onOmsRegisterConfig(config) }
    }

    test("should not throw if feature not found on RegisterConfigEvent") {
        val config = mockk<CLowTpsFeature>()

        every { CServer.features.getFeatureConfig<CLowTpsFeature>(any()) } returns config
        every { mockWeFeatureManager.getFeatureById<LowTpsFeature>(any()) } returns null

        // should run silently without error
        LowTpsOmsLifecycleEvents.onRegisterFeaturesConfigEvent(OMSLifecycle.Feature.RegisterConfigEvent())
    }
})