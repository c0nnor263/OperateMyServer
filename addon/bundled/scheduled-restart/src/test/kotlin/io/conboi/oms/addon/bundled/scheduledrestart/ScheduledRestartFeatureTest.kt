package io.conboi.oms.addon.bundled.scheduledrestart

import io.conboi.oms.addon.bundled.scheduledrestart.content.SkipResult
import io.conboi.oms.addon.bundled.scheduledrestart.foundation.reason.ScheduledStop
import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.text.ComponentStyles.bold
import io.conboi.oms.common.text.ComponentStyles.literal
import io.conboi.oms.testing.checkCapturedTranslationKey
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.players.PlayerList
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class ScheduledRestartFeatureTest : ShouldSpec({

    lateinit var sut: ScheduledRestartFeature
    val mockConfig = mockk<CScheduledRestartFeature>(relaxed = true)
    val mockConfigProvider: ConfigProvider<CScheduledRestartFeature> = mockk()
    val mockServer = mockk<MinecraftServer>(relaxed = true)
    val mockPlayers = mockk<PlayerList>(relaxed = true)
    val mockTickingEvent = mockk<OMSLifecycle.TickingEvent>(relaxed = true)
    val mockAddonContext = mockk<AddonContext>(relaxed = true)

    val nowEpoch = ZonedDateTime.now().withHour(5).withMinute(0).withSecond(0).withNano(0).toEpochSecond()

    val restartStrings = listOf("00:00", "06:00", "12:00", "18:00")
    val warningStrings = listOf(
        "2h", "30m", "15m", "10m",
        "5m", "2m", "1m",
        "30s", "15s", "10s", "5s", "4s", "3s", "2s", "1s"
    )

    beforeSpec {
        mockkObject(TimeHelper)
        mockkObject(FORGE_BUS)
    }

    beforeEach {
        every { FORGE_BUS.post(any()) } returns true
        every { TimeHelper.currentTime } returns nowEpoch
        every { mockServer.playerList } returns mockPlayers
        every { mockTickingEvent.server } returns mockServer

        every { mockConfigProvider.get() } returns mockConfig
        every { mockConfig.restartTimes.get() } returns restartStrings
        every { mockConfig.warningTimes.get() } returns warningStrings

        sut = ScheduledRestartFeature(mockConfigProvider).apply {
            onOmsRegisterConfig()
            restartTimes.get()
            warningTimes.get()
            restartTimeTarget.get()
        }
    }

    afterEach {
        clearAllMocks()
    }

    context("restartTimes") {

        should("parse and sort restart times") {
            val expected = restartStrings
                .map(LocalTime::parse)
                .sortedBy { it.toSecondOfDay() }

            sut.restartTimes.get() shouldContainExactly expected
        }

        should("validator reject empty and accept non-empty") {
            val validator = sut.restartTimes.validator!!
            validator(emptyList()) shouldBe false
            validator(listOf(LocalTime.NOON)) shouldBe true
        }

        should("mark config dirty after invalidation") {
            every { mockConfig.restartTimes.get() } returnsMany listOf(
                listOf("02:00"),
                listOf("03:00")
            )

            sut.isConfigDirty shouldBe false

            sut.restartTimes.invalidate()
            sut.restartTimes.get()

            sut.isConfigDirty shouldBe true
        }
    }

    context("warningTimes") {

        should("parse and sort descending") {
            val expected = warningStrings.map(Duration::parse)
                .sortedByDescending { it.inWholeSeconds }

            sut.warningTimes.get() shouldContainExactly expected
        }

        should("validator reject empty and accept non-empty") {
            val validator = sut.warningTimes.validator!!
            validator(emptyList()) shouldBe false
            validator(listOf(Duration.parse("5m"))) shouldBe true
        }
    }

    context("skip") {

        should("return Skipped on first call") {
            val current = sut.restartTimeTarget.get()
            val expectedNext = sut.nextRestartTimeTarget.get()

            sut.skip() shouldBe SkipResult.Skipped(
                skippedRestartTime = current,
                nextRestartTime = expectedNext
            )
        }

        should("return AlreadySkipped on second call") {
            sut.skip()
            val expectedNext = sut.nextRestartTimeTarget.get()

            sut.skip() shouldBe SkipResult.AlreadySkipped(nextRestartTime = expectedNext)
        }
    }

    context("onConfigUpdated") {

        should("reset skip state and recalc restart time") {
            sut.skip()
            sut.onConfigUpdated(mockTickingEvent)

            val newTarget = sut.restartTimeTarget.get()
            ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(newTarget),
                TimeHelper.zoneId
            ).toLocalTime() shouldBe LocalTime.of(6, 0)
        }

        should("send config updated message") {
            val slotMsg = slot<Component>()
            every { mockPlayers.broadcastSystemMessage(capture(slotMsg), false) } returns Unit

            sut.onConfigUpdated(mockTickingEvent)

            checkCapturedTranslationKey(
                slotMsg.captured,
                "oms.warning.autorestart.config_updated",
                "06:00".literal().bold()
            )
        }
    }

    context("handleWarnings") {

        should("broadcast matching warning") {
            val slotMsg = slot<Component>()

            sut.handleWarnings(300, mockServer)

            verify { mockPlayers.broadcastSystemMessage(capture(slotMsg), false) }
            checkCapturedTranslationKey(
                slotMsg.captured,
                "oms.warning.restart",
                "5m".literal().bold()
            )
        }
    }

    context("handleRestart") {

        should("post StopRequestedEvent when overdue") {
            val captured = slot<OMSActions.StopRequestedEvent>()

            sut.handleRestart(-5, mockServer)

            verify { FORGE_BUS.post(capture(captured)) }
            captured.captured.reason shouldBe ScheduledStop
        }

        should("do nothing for positive remainingSec") {
            sut.handleRestart(5, mockServer)
            verify(exactly = 0) { FORGE_BUS.post(any()) }
        }
    }

    context("pickClosestTarget") {

        should("return closest time today") {
            val now = ZonedDateTime.of(
                LocalDate.now(),
                LocalTime.of(12, 0),
                ZoneId.systemDefault()
            ).toEpochSecond()
            val expectedClosestTarget = LocalTime.of(13, 0)
            val result = sut.pickClosestTarget(
                listOf(expectedClosestTarget, LocalTime.of(15, 0)),
                now
            )

            ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(result),
                TimeHelper.zoneId
            ).toLocalTime() shouldBe expectedClosestTarget
        }
    }

    context("onOmsTick") {

        should("trigger restart when overdue") {
            val captured = slot<OMSActions.StopRequestedEvent>()

            every { TimeHelper.secondsBetween(any(), any()) } returns -1

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify { FORGE_BUS.post(capture(captured)) }
            captured.captured.reason shouldBe ScheduledStop
        }

        should("use nextRestartTimeTarget when skipped") {
            sut.skip()

            every { TimeHelper.secondsBetween(any(), any()) } returns -1

            val captured = slot<OMSActions.StopRequestedEvent>()
            every { FORGE_BUS.post(capture(captured)) } returns true

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify { FORGE_BUS.post(capture(captured)) }
            captured.isCaptured shouldBe true
        }
    }

    context("getFeatureCommands") {

        should("include Skip command") {
            sut.additionalCommands.map { it::class.simpleName } shouldContainExactly listOf(
                "ScheduledRestartFeatureSkipCommand"
            )
        }
    }

    context("info") {

        should("override id, priority and include restart_time snapshot") {
            val fixedEpoch = nowEpoch + 3600 // +1 hour

            every { TimeHelper.closest(any(), any()) } returns fixedEpoch

            sut = ScheduledRestartFeature(mockConfigProvider).apply {
                onOmsRegisterConfig()
                restartTimes.get()
                restartTimeTarget.invalidate()
            }

            val info = sut.info()

            info.id shouldBe CScheduledRestartFeature.NAME
            info.priority shouldBe Priority.COMMON

            val snapshot = info.data["restart_time"]

            snapshot shouldBe TimeFormatter.formatDateTime(sut.restartTimeTarget.getSnapshotSafely() ?: -1)
            snapshot shouldBe TimeFormatter.formatDateTime(fixedEpoch)
        }

        should("update restart_time snapshot after invalidation") {
            val firstEpoch = nowEpoch + 3600
            val secondEpoch = nowEpoch + 7200

            every { TimeHelper.closest(any(), any()) } returnsMany listOf(
                firstEpoch, secondEpoch
            )

            sut = ScheduledRestartFeature(mockConfigProvider).apply {
                onOmsRegisterConfig()
                restartTimes.get()
                restartTimeTarget.invalidate()
            }

            val info1 = sut.info()
            info1.data["restart_time"] shouldBe TimeFormatter.formatDateTime(firstEpoch)

            sut.restartTimeTarget.invalidate()

            val info2 = sut.info()
            info2.data["restart_time"] shouldBe TimeFormatter.formatDateTime(secondEpoch)
        }
    }

})
