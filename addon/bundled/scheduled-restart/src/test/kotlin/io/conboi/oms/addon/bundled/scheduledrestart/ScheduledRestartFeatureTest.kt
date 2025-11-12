package io.conboi.oms.addon.bundled.scheduledrestart


import io.conboi.oms.addon.bundled.scheduledrestart.content.SkipResult
import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.core.foundation.reason.ScheduledStop
import io.conboi.oms.utils.foundation.TimeFormatter
import io.conboi.oms.utils.foundation.TimeHelper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.unmockkObject
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.players.PlayerList
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class ScheduledRestartFeatureTest : FunSpec({

    lateinit var feature: ScheduledRestartFeature
    val mockConfig = mockk<CScheduledRestartFeature>(relaxed = true)
    val mockServer = mockk<MinecraftServer>(relaxed = true)
    val mockPlayers = mockk<PlayerList>(relaxed = true)

    val now = ZonedDateTime.now().withHour(5).withMinute(0).withSecond(0).withNano(0)
    val restartStrings = listOf("00:00", "06:00", "12:00", "18:00")
    val warningStrings =
        listOf("2h", "30m", "15m", "10m", "5m", "2m", "1m", "30s", "15s", "10s", "5s", "4s", "3s", "2s", "1s")

    beforeEach {
        mockkObject(TimeHelper, TimeFormatter)

        every { TimeHelper.currentTime } returns now
        every { mockConfig.restartTimes.get() } returns restartStrings
        every { mockConfig.warningTimes.get() } returns warningStrings

        every { TimeFormatter.parseToLocalTimeOrNull(any()) } answers { LocalTime.parse(it.invocation.args[0] as String) }
        every { TimeFormatter.parseToDurationOrNull(any()) } answers { Duration.parse(it.invocation.args[0] as String) }

        every { TimeHelper.convertLocalTimeToZonedDateTime(any(), any()) } answers {
            now.with(it.invocation.args[1] as LocalTime)
        }
        every { TimeHelper.closest(any(), any()) } answers {
            (it.invocation.args[1] as List<ZonedDateTime>).minByOrNull { dt -> kotlin.math.abs(dt.toEpochSecond() - now.toEpochSecond()) }
        }

        every { mockServer.playerList } returns mockPlayers

        feature = ScheduledRestartFeature().apply {
            onOmsRegisterConfig(mockConfig)
            restartTimes.get()
            warningTimes.get()
            restartTimeTarget.get()
        }
    }

    afterEach {
        clearAllMocks()
        unmockkAll()
    }

    context("restartTimes") {
        test("should parse and sort restart times") {
            val expected = restartStrings.map { LocalTime.parse(it) }.sortedBy { it.toSecondOfDay() }
            feature.restartTimes.get() shouldContainExactly expected
        }

        test("should reject empty and accept non-empty values") {
            val validator = feature.restartTimes.validator!!
            validator(emptyList()) shouldBe false
            validator(listOf(LocalTime.NOON)) shouldBe true
        }

        test("should mark config updated after restartTimes invalidation") {
            val feature = ScheduledRestartFeature()
            every { mockConfig.restartTimes.get() } returnsMany listOf(
                listOf("02:00"), listOf("03:00")
            )

            feature.onOmsRegisterConfig(mockConfig)
            feature.restartTimes.get()
            feature.isConfigurationUpdated shouldBe false

            feature.restartTimes.invalidate()
            feature.restartTimes.get()
            feature.isConfigurationUpdated shouldBe true
        }
    }

    context("warningTimes") {
        test("should parse and sort warning times descending") {
            val expected = warningStrings.map { Duration.parse(it) }.sortedByDescending { it.inWholeSeconds }
            feature.warningTimes.get() shouldContainExactly expected
        }

        test("should reject empty and accept non-empty values") {
            val validator = feature.warningTimes.validator!!
            validator(emptyList()) shouldBe false
            validator(listOf(5.minutes)) shouldBe true
        }
    }

    context("skip") {
        test("should return Skipped on first call") {
            val current = feature.restartTimeTarget.get()
            val result = feature.skip()
            result shouldBe SkipResult.Skipped(current, feature.getNextRestartTime())
        }

        test("should return AlreadySkipped on second call") {
            feature.skip()
            val result = feature.skip()
            result shouldBe SkipResult.AlreadySkipped(feature.getNextRestartTime())
        }
    }

    context("onConfigUpdated") {
        test("should reset restart target if skip was called") {
            val event = mockk<OMSLifecycle.TickingEvent> { every { server } returns mockServer }

            feature.skip()
            feature.onConfigUpdated(event)

            val newTarget = feature.restartTimeTarget.get()
            newTarget.toLocalTime() shouldBe LocalTime.of(6, 0)
        }

        test("should send config update message if not skipped") {
            val event = mockk<OMSLifecycle.TickingEvent> { every { server } returns mockServer }

            feature.onConfigUpdated(event)

            verify {
                mockPlayers.broadcastSystemMessage(
                    withArg { comp ->
                        val contents = (comp as MutableComponent).contents as TranslatableContents
                        contents.key shouldBe "oms.warning.autorestart.config_updated"
                    },
                    eq(false)
                )
            }
        }
    }

    context("handleWarnings") {
        test("should broadcast matching warning") {
            val captured = slot<Component>()
            feature.handleWarnings(300L, mockServer)

            verify { mockPlayers.broadcastSystemMessage(capture(captured), false) }

            val translatable = (captured.captured as MutableComponent).contents as TranslatableContents
            translatable.key shouldBe "oms.warning.restart"
            translatable.args[0] shouldBe "5m"
        }

        test("should suppress warning when skipped") {
            feature.skip()
            feature.handleWarnings(10L, mockServer)
            verify(exactly = 0) { mockPlayers.broadcastSystemMessage(any(), any()) }
        }
    }

    context("handleRestart") {
        test("should post StopRequestedEvent when overdue") {
            val past = now.minusSeconds(5)

            every { TimeHelper.currentTime } returns now
            every { mockConfig.restartTimes.get() } returns listOf(past.toLocalTime().toString())
            every { TimeHelper.convertLocalTimeToZonedDateTime(any(), any()) } returns past
            every { TimeHelper.closest(any(), any()) } returns past

            feature = ScheduledRestartFeature().apply {
                onOmsRegisterConfig(mockConfig)
                restartTimes.get()
                restartTimeTarget.invalidate()
            }

            mockkObject(FORGE_BUS)
            val captured = slot<OMSLifecycle.StopRequestedEvent>()
            every { FORGE_BUS.post(capture(captured)) } returns true

            feature.handleRestart(-5, mockServer)

            captured.isCaptured shouldBe true
            captured.captured.server shouldBe mockServer

            unmockkObject(FORGE_BUS)
        }

        test("should do nothing if skipped") {
            feature.skip()
            feature.handleRestart(-5, mockServer)
        }

        test("should do nothing if remainingSec is positive") {
            feature.handleRestart(5, mockServer)
        }
    }

    context("pickClosestTarget") {
        test("should return closest time today") {
            val now = ZonedDateTime.of(LocalDate.now(), LocalTime.of(12, 0), ZoneId.of("Europe/Kiev"))
            val result = feature.pickClosestTarget(listOf(LocalTime.of(13, 0), LocalTime.of(15, 0)), now)

            result.toLocalTime() shouldBe LocalTime.of(13, 0)
        }

        test("should return time from next day if none left today") {
            val now = ZonedDateTime.of(LocalDate.now(), LocalTime.of(23, 59), ZoneId.of("Europe/Kiev"))
            val times = listOf(LocalTime.of(0, 0))
            val tomorrow = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)

            mockkObject(TimeHelper)
            every { TimeHelper.closest(any(), any()) } returnsMany listOf(null, tomorrow)
            every { TimeHelper.convertLocalTimeToZonedDateTime(any(), any()) } answers {
                (it.invocation.args[0] as ZonedDateTime).with(it.invocation.args[1] as LocalTime)
            }

            val result = feature.pickClosestTarget(times, now)

            result.toLocalDate() shouldBe now.toLocalDate().plusDays(1)
            result.toLocalTime() shouldBe LocalTime.of(0, 0)

            unmockkObject(TimeHelper)
        }

        test("should throw if no time found") {
            mockkObject(TimeHelper)
            every { TimeHelper.closest(any(), any()) } returns null
            every { TimeHelper.convertLocalTimeToZonedDateTime(any(), any()) } answers {
                (it.invocation.args[0] as ZonedDateTime).with(it.invocation.args[1] as LocalTime)
            }

            shouldThrow<IllegalStateException> {
                feature.pickClosestTarget(emptyList(), now, maxDaysLookahead = 0)
            }.message shouldBe "cannot pick closest target"

            unmockkObject(TimeHelper)
        }
    }

    context("onOmsTick") {
        test("should post StopRequestedEvent with reason ScheduledStop") {
            val past = now.minusSeconds(5)

            every { TimeHelper.currentTime } returns now
            every { TimeHelper.secondsBetween(now, past) } returns -5
            every { mockConfig.restartTimes.get() } returns listOf(past.toLocalTime().toString())
            every { TimeHelper.convertLocalTimeToZonedDateTime(any(), any()) } returns past
            every { TimeHelper.closest(any(), any()) } returns past

            feature = ScheduledRestartFeature().apply {
                onOmsRegisterConfig(mockConfig)
                restartTimes.get()
                restartTimeTarget.invalidate()
            }

            val event = mockk<OMSLifecycle.TickingEvent> {
                every { server } returns mockServer
            }

            mockkObject(FORGE_BUS)
            val captured = slot<OMSLifecycle.StopRequestedEvent>()
            every { FORGE_BUS.post(capture(captured)) } returns true

            feature.onOmsTick(event)

            captured.isCaptured shouldBe true
            captured.captured.reason shouldBe ScheduledStop

            unmockkObject(FORGE_BUS)
        }
    }

    context("getFeatureCommands") {
        test("should include skip command") {
            val commands = feature.getFeatureCommands()
            commands.map { it::class.simpleName } shouldContainExactly listOf("ScheduledRestartFeatureSkipCommand")
        }
    }
})