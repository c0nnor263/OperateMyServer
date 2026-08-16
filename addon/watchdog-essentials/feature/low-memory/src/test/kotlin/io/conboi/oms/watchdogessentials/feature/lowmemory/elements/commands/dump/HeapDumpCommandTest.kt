package io.conboi.oms.watchdogessentials.feature.lowmemory.elements.commands.dump

import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.common.infrastructure.lang.OmsLang
import io.conboi.oms.testing.captureFail
import io.conboi.oms.testing.captureSuccess
import io.conboi.oms.testing.checkCapturedTranslationKey
import io.conboi.oms.watchdogessentials.feature.lowmemory.LowMemoryFeature
import io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.config.CLowMemoryFeature
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.minecraft.commands.CommandSourceStack


class HeapDumpCommandTest : ShouldSpec({

    lateinit var sut: HeapDumpCommand

    val mockContext = mockk<CommandContext<CommandSourceStack>>(relaxed = true)
    val mockSource = mockk<CommandSourceStack>(relaxed = true)
    val mockFeature = mockk<LowMemoryFeature>(relaxed = true)
    val mockFeatureInfo = mockk<FeatureInfo>(relaxed = true)

    beforeEach {
        every { mockContext.source } returns mockSource
        every { mockFeatureInfo.id } returns CLowMemoryFeature.NAME
        every { mockFeature.info() } returns mockFeatureInfo

        sut = HeapDumpCommand(mockFeature)
    }

    afterEach {
        clearAllMocks()
    }

    context("disabled feature") {

        should("return 0 and send not_enabled when feature is disabled") {
            every { mockFeature.isEnabled() } returns false

            val slotFail = captureFail(mockSource)
            val result = sut.requestHeapDump(mockContext)

            result shouldBe 0

            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.feature.not_enabled",
                OmsLang.translatable(CLowMemoryFeature.NAME)
            )
        }
    }

    context("Command.SINGLE_SUCCESS") {
        should("send success message when heap dump is requested") {
            every { mockFeature.isEnabled() } returns true

            val slotSuccess = captureSuccess(mockSource)
            val result = sut.requestHeapDump(mockContext)

            result shouldBe 1

            checkCapturedTranslationKey(
                slotSuccess.captured.get(),
                "watchdogessentials.command.low_memory.heapdump.requested"
            )
        }
    }
})
