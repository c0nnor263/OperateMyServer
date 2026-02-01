package io.conboi.oms.watchdogessentials.addon.emptyserverrestart

import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.event.EmptyServerRestartEvents
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.reason.EmptyServerRestartStop
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
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
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class EmptyServerRestartFeatureTest : ShouldSpec({

    lateinit var sut: EmptyServerRestartFeature
    val mockConfig = mockk<CEmptyServerRestartFeature>(relaxed = true)
    val mockConfigProvider: ConfigProvider<CEmptyServerRestartFeature> = mockk()
    val mockServer = mockk<MinecraftServer>(relaxed = true)
    val mockTickEvent = mockk<OMSLifecycle.TickingEvent>(relaxed = true)
    val mockAddonContext = mockk<AddonContext>(relaxed = true)


    beforeSpec {
        mockkObject(TimeHelper)
        mockkObject(FORGE_BUS)
        mockkObject(LOG)
    }

    beforeEach {
        every { mockTickEvent.server } returns mockServer
        every { mockConfig.countTime.get() } returns "20s"
        every { mockConfigProvider.get() } returns mockConfig

        sut = EmptyServerRestartFeature(mockConfigProvider)
        sut.onOmsRegisterConfig()
    }

    afterEach {
        clearAllMocks()
    }

    context("countTime") {

        should("cache and return parsed duration") {
            sut.countTime.get() shouldBe 20.toDuration(DurationUnit.SECONDS)

            every { mockConfig.countTime.get() } returns "5s"
            sut.countTime.invalidate()

            sut.countTime.get() shouldBe 5.toDuration(DurationUnit.SECONDS)
        }

        should("throw if duration cannot be parsed") {
            every { mockConfig.countTime.get() } returns "???"

            val ex = shouldThrow<IllegalStateException> {
                sut.countTime.invalidate()
            }
            ex.message shouldBe "Cannot parse countTime"
        }
    }

    context("onOmsTick") {

        should("do nothing if emptyServerTime is null") {
            sut.clearTime()

            sut.onOmsTick(mockTickEvent, mockAddonContext)

            verify(exactly = 0) { FORGE_BUS.post(any<OMSActions.StopRequestedEvent>()) }
        }

        should("not trigger StopRequestedEvent if not enough time passed") {
            val startEpoch = 1000L
            val nowEpoch = 1010L

            every { TimeHelper.currentTime } returns startEpoch
            sut.initTime()

            every { TimeHelper.currentTime } returns nowEpoch
            every { TimeHelper.secondsBetween(startEpoch, nowEpoch) } returns 10L

            sut.onOmsTick(mockTickEvent, mockAddonContext)

            verify(exactly = 0) { FORGE_BUS.post(any()) }
        }

        should("trigger StopRequestedEvent if enough time passed") {
            val startEpoch = 1000L
            val nowEpoch = 2000L

            every { TimeHelper.currentTime } returns startEpoch
            sut.initTime()

            every { TimeHelper.currentTime } returns nowEpoch
            every { TimeHelper.secondsBetween(startEpoch, nowEpoch) } returns 1000L

            every { LOG.info(any<String>()) } just Runs
            val slot = slot<OMSActions.StopRequestedEvent>()
            every { FORGE_BUS.post(capture(slot)) } returns true

            sut.onOmsTick(mockTickEvent, mockAddonContext)

            slot.isCaptured shouldBe true
            slot.captured.server shouldBe mockServer
            slot.captured.reason shouldBe EmptyServerRestartStop
        }
    }

    context("initTime") {
        should("store current time") {
            val start = 5000L
            every { TimeHelper.currentTime } returns start

            sut.initTime()

            every { TimeHelper.currentTime } returns 5001L
            every { TimeHelper.secondsBetween(start, 5001L) } returns 1L

            sut.onOmsTick(mockTickEvent, mockAddonContext)

            verify(exactly = 0) { FORGE_BUS.post(any()) }
        }
    }

    context("clearTime") {
        should("reset timer") {
            val start = 6000L

            every { TimeHelper.currentTime } returns start
            sut.initTime()

            sut.clearTime()
            sut.onOmsTick(mockTickEvent, mockAddonContext)

            verify(exactly = 0) { FORGE_BUS.post(any()) }
        }
    }

    context("info") {

        should("override id and priority, but preserve data map") {
            val a = sut.info()
            a.id shouldBe CEmptyServerRestartFeature.NAME
            a.priority shouldBe Priority.COMMON

            val b = sut.info()
            b.id shouldBe CEmptyServerRestartFeature.NAME
            b.priority shouldBe Priority.COMMON

            a.data shouldBe b.data

            (a === b) shouldBe false
        }
    }

    context("createEventListeners") {
        should("return list with EmptyServerRestartEvents") {
            val listeners = sut.createEventListeners()
            listeners.size shouldBe 1
            listeners[0].shouldBeInstanceOf<EmptyServerRestartEvents>()
        }
    }
})
