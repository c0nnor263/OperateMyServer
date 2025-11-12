package io.conboi.oms.watchdogessentials.addon.lowtps

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.utils.foundation.TimeFormatter
import io.conboi.oms.watchdogessentials.addon.lowtps.foundation.TpsMonitor
import io.conboi.oms.watchdogessentials.addon.lowtps.foundation.reason.LowTpsStop
import io.conboi.oms.watchdogessentials.addon.lowtps.infrastructure.config.CLowTpsFeature
import io.conboi.oms.watchdogessentials.core.infrastructure.LOG
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlin.time.Duration.Companion.seconds
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class LowTpsFeatureTest : FunSpec({

    lateinit var feature: LowTpsFeature
    lateinit var config: CLowTpsFeature
    lateinit var server: MinecraftServer
    lateinit var event: OMSLifecycle.TickingEvent

    beforeEach {
        mockkObject(TimeFormatter, TpsMonitor, FORGE_BUS)
        mockkObject(LOG)

        config = mockk(relaxed = true)
        server = mockk(relaxed = true)
        event = mockk {
            every { this@mockk.server } returns server
        }

        every { config.tpsCountTime.get() } returns "10s"
        every { config.tpsThreshold.get() } returns 19
        every { TimeFormatter.parseToDurationOrNull("10s") } returns 10.seconds

        feature = LowTpsFeature()
        feature.onOmsRegisterConfig(config)
    }

    afterEach {
        unmockkAll()
    }

    context("tpsCountTime") {

        test("should parse tpsCountTime once and cache") {
            feature.tpsCountTime.get() shouldBe 10.seconds

            every { config.tpsCountTime.get() } returns "5s"
            every { TimeFormatter.parseToDurationOrNull("5s") } returns 5.seconds

            feature.tpsCountTime.invalidate()
            feature.tpsCountTime.get() shouldBe 5.seconds
        }

        test("should throw if tpsCountTime cannot be parsed") {
            every { config.tpsCountTime.get() } returns "??"
            every { TimeFormatter.parseToDurationOrNull("??") } returns null

            val ex = shouldThrow<IllegalStateException> {
                feature.tpsCountTime.invalidate()
            }
            ex.message shouldBe "Cannot parse tpsCountTime"
        }
    }

    context("onOmsTick") {

        test("should do nothing if TPS is above threshold") {
            every { TpsMonitor.update(server) } just Runs
            every { TpsMonitor.averageTpsOver(10.seconds) } returns 20.0

            feature.onOmsTick(event)

            verify(exactly = 0) { FORGE_BUS.post(any<OMSLifecycle.StopRequestedEvent>()) }
        }

        test("should post StopRequestedEvent if TPS is low") {
            every { TpsMonitor.update(server) } just Runs
            every { TpsMonitor.averageTpsOver(10.seconds) } returns 18.0

            val slot = slot<OMSLifecycle.StopRequestedEvent>()
            every { FORGE_BUS.post(capture(slot)) } returns true

            feature.onOmsTick(event)

            slot.isCaptured shouldBe true
            slot.captured.server shouldBe server
            slot.captured.reason shouldBe LowTpsStop
        }
    }
})