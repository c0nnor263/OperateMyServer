package io.conboi.oms.watchdogessentials.feature.lowmemory.elements.commands.summary

import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.lang.OmsLang
import io.conboi.oms.testing.captureFail
import io.conboi.oms.testing.captureSuccess
import io.conboi.oms.testing.checkCapturedTranslationKey
import io.conboi.oms.watchdogessentials.feature.lowmemory.LowMemoryFeature
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.ByteFormatter
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.RetentionSummary
import io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.config.CLowMemoryFeature
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

class SummaryOverCommandTest : ShouldSpec({

    lateinit var sut: SummaryOverCommand

    val mockContext = mockk<CommandContext<CommandSourceStack>>(relaxed = true)
    val mockSource = mockk<CommandSourceStack>(relaxed = true)
    val mockFeature = mockk<LowMemoryFeature>(relaxed = true)
    val mockFeatureInfo = mockk<FeatureInfo>(relaxed = true)

    beforeEach {
        every { mockContext.source } returns mockSource
        every { mockFeatureInfo.id } returns CLowMemoryFeature.NAME
        every { mockFeature.info() } returns mockFeatureInfo

        sut = SummaryOverCommand(mockFeature)
    }

    afterEach {
        clearAllMocks()
    }

    context("disabled feature") {
        should("return 0 and send not_enabled when feature is disabled") {
            every { mockFeature.isEnabled() } returns false

            val slotFail = captureFail(mockSource)
            val result = sut.requestSummaryOver(mockContext, null)

            result shouldBe 0

            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.feature.not_enabled",
                OmsLang.translatable(CLowMemoryFeature.NAME)
            )
        }
    }

    context("default window") {
        should("send default window message and summary when window is omitted") {
            every { mockFeature.isEnabled() } returns true
            every { mockFeature.averagingWindow } returns 5.minutes
            every { mockFeature.requestSummaryOver(5.minutes) } returns mockSummary()

            val systemSlot = slot<Component>()
            every { mockSource.sendSystemMessage(capture(systemSlot)) } just Runs

            val slotSuccess = captureSuccess(mockSource)
            val result = sut.requestSummaryOver(mockContext, null)

            result shouldBe 1

            checkCapturedTranslationKey(
                systemSlot.captured,
                "watchdogessentials.command.low_memory.summary_over.default_window",
                TimeFormatter.formatDuration(5.minutes)
            )

            slotSuccess.captured.get().string shouldBe expectedSummaryString()
        }
    }

    context("invalid window") {
        should("return 0 and send invalid_window when window cannot be parsed") {
            every { mockFeature.isEnabled() } returns true
            every { mockFeature.averagingWindow } returns 5.minutes

            val slotFail = captureFail(mockSource)
            val result = sut.requestSummaryOver(mockContext, "bad-window")

            result shouldBe 0

            checkCapturedTranslationKey(
                slotFail.captured,
                "watchdogessentials.command.low_memory.summary_over.invalid_window",
                "bad-window"
            )
        }

        should("return 0 and send non_positive_window when window is not positive") {
            every { mockFeature.isEnabled() } returns true
            every { mockFeature.averagingWindow } returns 5.minutes

            val slotFail = captureFail(mockSource)
            val result = sut.requestSummaryOver(mockContext, "0s")

            result shouldBe 0

            checkCapturedTranslationKey(
                slotFail.captured,
                "watchdogessentials.command.low_memory.summary_over.non_positive_window",
                "0s"
            )
        }
    }

    context("window exceeds max") {
        should("send warning message and summary when window is above max") {
            val defaultRetentionWindow = 5.minutes
            every { mockFeature.isEnabled() } returns true
            every { mockFeature.averagingWindow } returns defaultRetentionWindow
            every { mockFeature.requestSummaryOver(5.minutes) } returns mockSummary(
                retentionWindow = 5.minutes
            )

            val systemSlot = slot<Component>()
            every { mockSource.sendSystemMessage(capture(systemSlot)) } just Runs

            val slotSuccess = captureSuccess(mockSource)
            val result = sut.requestSummaryOver(mockContext, "1h")

            result shouldBe 1

            checkCapturedTranslationKey(
                systemSlot.captured,
                "watchdogessentials.command.low_memory.summary_over.window_exceeds_max",
                TimeFormatter.formatDuration(1.hours),
                TimeFormatter.formatDuration(defaultRetentionWindow)
            )

            slotSuccess.captured.get().string shouldBe expectedSummaryString(retentionWindow = defaultRetentionWindow)
        }
    }
})

private fun mockSummary(
    retentionWindow: Duration = 5.minutes
): RetentionSummary {
    return RetentionSummary(
        snapshotsCount = 3,
        retentionWindow = retentionWindow,
        averageUsedBytes = 1024L,
        averageAvailableBytes = 2048L,
        averageUsedPercent = 25.0,
        averageAvailablePercent = 50.0,
        minAvailableBytes = 512L,
        maxUsedBytes = 1536L
    )
}

private fun expectedSummaryString(
    retentionWindow: Duration = 5.minutes
): String {
    return "=== Low Memory History ===\n" +
        "\nWindow\n" +
        "Duration: ".padEnd(20) + "\n" +
        "${TimeFormatter.formatDuration(retentionWindow)}\n" +
        "Snapshots: ".padEnd(20) + "\n" +
        "3\n" +
        "\nAverage\n" +
        "Used: ".padEnd(20) + "\n" +
        "${SummaryCommand.formatBytesPercent(1024L, 25.0).string}\n" +
        "Available: ".padEnd(20) + "\n" +
        "${SummaryCommand.formatBytesPercent(2048L, 50.0).string}\n" +
        "\nExtremes\n" +
        "Highest Used: ".padEnd(20) + "\n" +
        "${ByteFormatter.format(1536L)}\n" +
        "Lowest Available: ".padEnd(20) + "\n" +
            ByteFormatter.format(512L)
}
