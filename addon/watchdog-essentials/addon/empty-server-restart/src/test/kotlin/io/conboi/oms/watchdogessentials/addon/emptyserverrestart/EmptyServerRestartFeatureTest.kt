package io.conboi.oms.watchdogessentials.addon.emptyserverrestart

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.utils.foundation.TimeFormatter
import io.conboi.oms.utils.foundation.TimeHelper
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.reason.EmptyServerRestartStop
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
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
import java.time.ZonedDateTime
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class EmptyServerRestartFeatureTest : FunSpec({

    lateinit var feature: EmptyServerRestartFeature
    lateinit var config: CEmptyServerRestartFeature
    lateinit var server: MinecraftServer
    lateinit var event: OMSLifecycle.TickingEvent

    beforeEach {
        mockkObject(TimeFormatter, TimeHelper, FORGE_BUS, LOG)

        config = mockk(relaxed = true)
        server = mockk(relaxed = true)
        event = mockk {
            every { this@mockk.server } returns server
        }

        every { config.countTime.get() } returns "20s"
        every { TimeFormatter.parseToDurationOrNull("20s") } returns 20.toDuration(DurationUnit.SECONDS)

        feature = EmptyServerRestartFeature()
        feature.onOmsRegisterConfig(config)
    }

    afterEach {
        unmockkAll()
    }

    context("countTime") {

        test("should cache and return parsed duration") {
            feature.countTime.get() shouldBe 20.toDuration(DurationUnit.SECONDS)

            every { config.countTime.get() } returns "5s"
            every { TimeFormatter.parseToDurationOrNull("5s") } returns 5.toDuration(DurationUnit.SECONDS)

            feature.countTime.invalidate()
            feature.countTime.get() shouldBe 5.toDuration(DurationUnit.SECONDS)
        }

        test("should throw if duration cannot be parsed") {
            every { config.countTime.get() } returns "???"
            every { TimeFormatter.parseToDurationOrNull("???") } returns null

            val ex = shouldThrow<IllegalStateException> {
                feature.countTime.invalidate()
            }

            ex.message shouldBe "Cannot parse countTime"
        }
    }

    context("onOmsTick") {

        test("should do nothing if emptyServerTime is null") {
            feature.clearTime()

            feature.onOmsTick(event)

            verify(exactly = 0) { FORGE_BUS.post(any<OMSLifecycle.StopRequestedEvent>()) }
        }

        test("should not trigger StopRequestedEvent if not enough time passed") {
            val startTime = mockk<ZonedDateTime>()
            val nowTime = mockk<ZonedDateTime>()

            every { TimeHelper.currentTime } returns startTime
            feature.initTime()

            every { TimeHelper.currentTime } returns nowTime
            every { TimeHelper.secondsBetween(startTime, nowTime) } returns 10L

            feature.onOmsTick(event)

            verify(exactly = 0) { FORGE_BUS.post(any<OMSLifecycle.StopRequestedEvent>()) }
        }

        test("should trigger StopRequestedEvent if enough time passed") {
            val startTime = mockk<ZonedDateTime>()
            val nowTime = mockk<ZonedDateTime>()
            val slot = slot<OMSLifecycle.StopRequestedEvent>()

            every { TimeHelper.currentTime } returns startTime
            feature.initTime()

            every { TimeHelper.currentTime } returns nowTime
            every { TimeHelper.secondsBetween(startTime, nowTime) } returns 25L
            every { LOG.info(any<String>()) } just Runs
            every { FORGE_BUS.post(capture(slot)) } returns true

            feature.onOmsTick(event)

            slot.isCaptured shouldBe true
            slot.captured.server shouldBe server
            slot.captured.reason shouldBe EmptyServerRestartStop
        }
    }

    context("timer control") {

        test("initTime should store current time") {
            val mockTime = mockk<ZonedDateTime>()
            every { TimeHelper.currentTime } returns mockTime

            feature.initTime()

            every { TimeHelper.currentTime } returns mockk()
            every { TimeHelper.secondsBetween(mockTime, any()) } returns 0L

            feature.onOmsTick(event)

            verify(exactly = 0) { FORGE_BUS.post(any()) }
        }

        test("clearTime should reset timer") {
            every { TimeHelper.currentTime } returns mockk()
            feature.initTime()
            feature.clearTime()

            every { TimeHelper.currentTime } returns mockk()
            feature.onOmsTick(event)

            verify(exactly = 0) { FORGE_BUS.post(any<OMSLifecycle.StopRequestedEvent>()) }
        }
    }
})