package io.conboi.oms.watchdogessentials.feature.lowmemory.elements.commands.summary

import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.testing.captureFail
import io.conboi.oms.testing.captureSuccess
import io.conboi.oms.testing.checkCapturedTranslationKey
import io.conboi.oms.watchdogessentials.feature.lowmemory.LowMemoryFeature
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.ByteFormatter
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemorySnapshot
import io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.config.CLowMemoryFeature
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

class SummaryCommandTest : ShouldSpec({

    lateinit var sut: SummaryCommand

    val mockContext = mockk<CommandContext<CommandSourceStack>>(relaxed = true)
    val mockSource = mockk<CommandSourceStack>(relaxed = true)
    val mockFeature = mockk<LowMemoryFeature>(relaxed = true)
    val mockFeatureInfo = mockk<FeatureInfo>(relaxed = true)

    beforeEach {
        every { mockContext.source } returns mockSource
        every { mockFeatureInfo.id } returns CLowMemoryFeature.NAME
        every { mockFeature.info() } returns mockFeatureInfo

        sut = SummaryCommand(mockFeature)
    }

    afterEach {
        clearAllMocks()
    }

    context("disabled feature") {
        should("return 0 and send not_enabled when feature is disabled") {
            every { mockFeature.isEnabled() } returns false

            val slotFail = captureFail(mockSource)
            val result = sut.requestSummary(mockContext)

            result shouldBe 0

            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.feature.not_enabled",
                Component.translatable(CLowMemoryFeature.NAME)
            )
        }
    }

    context("Command.SINGLE_SUCCESS") {
        should("send empty summary when no snapshot is available") {
            every { mockFeature.isEnabled() } returns true
            every { mockFeature.requestSummary() } returns null

            val slotSuccess = captureSuccess(mockSource)
            val result = sut.requestSummary(mockContext)

            result shouldBe 1

            slotSuccess.captured.get().string shouldBe
                    "=== Low Memory Summary ===\n\nCurrent\nNo memory snapshots available yet.\n"
        }

        should("send summary with current snapshot details") {
            val mockSnapshot = MemorySnapshot(
                createdAt = 1L,
                maxBytes = 1024L,
                allocatedBytes = 1024L,
                freeAllocatedBytes = 0L
            )
            every { mockFeature.isEnabled() } returns true
            every { mockFeature.requestSummary() } returns mockSnapshot
            val usedExpected = SummaryCommand.formatBytesPercent(mockSnapshot.usedBytes, mockSnapshot.usedPercent).string
            val availableExpected = SummaryCommand.formatBytesPercent(mockSnapshot.availableBytes, mockSnapshot.availablePercent).string
            val allocatedExpected = ByteFormatter.format(mockSnapshot.allocatedBytes)
            val maxExpected = ByteFormatter.format(mockSnapshot.maxBytes)

            val slotSuccess = captureSuccess(mockSource)
            val result = sut.requestSummary(mockContext)

            result shouldBe 1

            slotSuccess.captured.get().string shouldBe
                    "=== Low Memory Summary ===\n" +
                    "\nCurrent\n" +
                    "Used: ".padEnd(20) + "\n" +
                    "$usedExpected\n" +
                    "Available: ".padEnd(20) + "\n" +
                    "$availableExpected\n" +
                    "Allocated: ".padEnd(20) + "\n" +
                    "$allocatedExpected\n" +
                    "Maximum Heap: ".padEnd(20) + "\n" +
                    "$maxExpected\n"
        }
    }
})
