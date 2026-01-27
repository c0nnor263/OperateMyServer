package io.conboi.oms.watchdogessentials.addon.lowtps

import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.watchdogessentials.addon.lowtps.foundation.TpsMonitor
import io.conboi.oms.watchdogessentials.addon.lowtps.foundation.reason.LowTpsStop
import io.conboi.oms.watchdogessentials.addon.lowtps.infrastructure.config.CLowTpsFeature
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
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
        mockkObject(TpsMonitor)
        mockkObject(FORGE_BUS)
        mockkObject(LOG)
    }

    beforeEach {
        every { TpsMonitor.update(mockServer) } just Runs
        every { LOG.warn(any<String>()) } just Runs
        every { mockTickingEvent.server } returns mockServer

        every { mockConfig.tpsCountTime.get() } returns "10s"
        every { mockConfig.tpsThreshold.get() } returns 15
        every { mockConfigProvider.get() } returns mockConfig

        sut = LowTpsFeature(mockConfigProvider)
        sut.onOmsRegisterConfig()
    }

    afterEach {
        clearAllMocks()
    }

    context("tpsCountTime") {

        should("cache parsed duration") {
            sut.tpsCountTime.get() shouldBe 10.seconds

            every { mockConfig.tpsCountTime.get() } returns "5s"
            sut.tpsCountTime.invalidate()

            sut.tpsCountTime.get() shouldBe 5.seconds
        }

        should("throw if cannot parse tpsCountTime") {
            every { mockConfig.tpsCountTime.get() } returns "??"

            val ex = shouldThrow<IllegalStateException> {
                sut.tpsCountTime.invalidate()
            }
            ex.message shouldBe "Cannot parse tpsCountTime"
        }
    }

    context("onOmsTick") {

        should("not trigger StopRequestedEvent if TPS above threshold") {
            every { TpsMonitor.averageTpsOver(10.seconds) } returns 20.0

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 0) { FORGE_BUS.post(any<OMSActions.StopRequestedEvent>()) }
        }

        should("not trigger StopRequestedEvent if TPS is exactly threshold") {
            every { TpsMonitor.averageTpsOver(10.seconds) } returns 15.0

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 0) { FORGE_BUS.post(any()) }
        }

        should("trigger StopRequestedEvent when TPS below threshold") {
            every { TpsMonitor.averageTpsOver(10.seconds) } returns 14.0

            val slot = slot<OMSActions.StopRequestedEvent>()
            every { FORGE_BUS.post(capture(slot)) } returns true

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            slot.isCaptured shouldBe true
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