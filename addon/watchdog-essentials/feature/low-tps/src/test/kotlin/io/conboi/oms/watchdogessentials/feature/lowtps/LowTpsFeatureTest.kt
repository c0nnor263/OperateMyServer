package io.conboi.oms.watchdogessentials.feature.lowtps

import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import io.conboi.oms.watchdogessentials.feature.lowtps.foundation.TpsCalculator
import io.conboi.oms.watchdogessentials.feature.lowtps.foundation.reason.LowTpsStop
import io.conboi.oms.watchdogessentials.feature.lowtps.infrastructure.config.CLowTpsFeature
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import kotlin.time.Duration.Companion.seconds
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class LowTpsFeatureTest : ShouldSpec({

    lateinit var sut: LowTpsFeature

    val mockConfig: CLowTpsFeature = mockk(relaxed = true)
    val mockConfigProvider = mockk<ConfigProvider<CLowTpsFeature>>()
    val mockServer: MinecraftServer = mockk(relaxed = true)
    val mockTickingEvent: OMSLifecycle.TickingEvent = mockk()
    val mockAddonContext: AddonContext = mockk(relaxed = true)

    beforeSpec {
        mockkObject(TimeHelper)
        mockkObject(TpsCalculator)
        mockkObject(FORGE_BUS)
        mockkObject(LOG)
    }

    beforeEach {
        every { TimeHelper.currentEpochSeconds } returns 1_000L
        every { TpsCalculator.calculateGlobalTps(mockServer) } returns 20.0
        every { LOG.warn(any<String>()) } just Runs
        every { mockTickingEvent.server } returns mockServer

        every { mockConfig.tpsAveragingWindow.get() } returns "10s"
        every { mockConfig.tpsThreshold.get() } returns 15
        every { mockConfigProvider.get() } returns mockConfig

        sut = LowTpsFeature(mockConfigProvider)
        sut.onOmsRegisterConfig()
    }

    afterEach {
        clearAllMocks()
    }

    context("tpsAveragingWindow") {

        should("cache parsed duration") {
            sut.tpsAveragingWindow.get() shouldBe 10.seconds

            every { mockConfig.tpsAveragingWindow.get() } returns "5s"
            sut.tpsAveragingWindow.invalidate()

            sut.tpsAveragingWindow.get() shouldBe 5.seconds
        }

        should("throw if cannot parse tpsAveragingWindow") {
            every { mockConfig.tpsAveragingWindow.get() } returns "??"

            val ex = shouldThrow<IllegalStateException> {
                sut.tpsAveragingWindow.invalidate()
            }
            ex.message shouldBe "Cannot parse tpsAveragingWindow"
        }
    }

    context("onOmsTick") {

        should("not trigger StopRequestedEvent if TPS above threshold") {
            every { TpsCalculator.calculateGlobalTps(mockServer) } returns 20.0

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 0) { FORGE_BUS.post(any<OMSActions.StopRequestedEvent>()) }
        }

        should("not trigger StopRequestedEvent if TPS is exactly threshold") {
            every { TpsCalculator.calculateGlobalTps(mockServer) } returns 15.0

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 0) { FORGE_BUS.post(any()) }
        }

        should("trigger StopRequestedEvent when TPS below threshold") {
            every { TpsCalculator.calculateGlobalTps(mockServer) } returns 14.0

            val slot = slot<OMSActions.StopRequestedEvent>()
            every { FORGE_BUS.post(capture(slot)) } returns true

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            slot.isCaptured shouldBe true
            slot.captured.server shouldBe mockServer
            slot.captured.reason shouldBe LowTpsStop
        }

        should("not trigger StopRequestedEvent multiple times when TPS below threshold") {
            every { TpsCalculator.calculateGlobalTps(mockServer) } returns 14.0

            val slot = slot<OMSActions.StopRequestedEvent>()
            every { FORGE_BUS.post(capture(slot)) } returns true

            sut.onOmsTick(mockTickingEvent, mockAddonContext)
            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 1) {
                FORGE_BUS.post(any<OMSActions.StopRequestedEvent>())
            }

            slot.captured.server shouldBe mockServer
            slot.captured.reason shouldBe LowTpsStop
        }
    }

    context("info") {

        should("override id and priority") {
            val base = sut.info()
            val info = sut.info()

            info.id shouldBe CLowTpsFeature.NAME
            info.priority shouldBe Priority.CRITICAL

            info.data shouldBe base.data
        }

        should("return new copy each time") {
            val a = sut.info()
            val b = sut.info()

            a shouldBe b
            (a === b) shouldBe false
        }
    }
})