package io.conboi.oms.feature.scheduledrestart.elements.commands

import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.lang.OmsLang
import io.conboi.oms.common.text.ComponentStyles.bold
import io.conboi.oms.common.text.ComponentStyles.literal
import io.conboi.oms.feature.scheduledrestart.ScheduledRestartFeature
import io.conboi.oms.feature.scheduledrestart.foundation.SkipResult
import io.conboi.oms.feature.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.testing.captureFail
import io.conboi.oms.testing.captureSuccess
import io.conboi.oms.testing.checkCapturedTranslationKey
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.time.ZonedDateTime
import net.minecraft.commands.CommandSourceStack

class SkipCommandTest : ShouldSpec({

    lateinit var sut: SkipCommand

    val mockContext = mockk<CommandContext<CommandSourceStack>>(relaxed = true)
    val mockSource = mockk<CommandSourceStack>(relaxed = true)
    val mockFeature = mockk<ScheduledRestartFeature>(relaxed = true)
    val mockFeatureInfo = mockk<FeatureInfo>(relaxed = true)

    beforeEach {
        every { mockContext.source } returns mockSource
        every { mockFeatureInfo.id } returns CScheduledRestartFeature.NAME
        every { mockFeature.info() } returns mockFeatureInfo

        sut = SkipCommand(mockFeature)
    }

    afterEach {
        clearAllMocks()
    }

    context("disabled feature") {

        should("return 0 and send not_enabled when feature is disabled") {
            every { mockFeature.isEnabled() } returns false

            val slotFail = captureFail(mockSource)
            val result = sut.skip(mockContext)

            result shouldBe 0

            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.feature.not_enabled",
                OmsLang.translatable(CScheduledRestartFeature.NAME)
            )
        }
    }

    context("Command.SINGLE_SUCCESS") {

        should("send success message when skip returns Skipped") {
            every { mockFeature.isEnabled() } returns true

            val now = ZonedDateTime.now()
            val skipped = now.plusHours(1).toEpochSecond()
            val next = now.plusHours(3).toEpochSecond()

            every { mockFeature.skip() } returns SkipResult.Skipped(skipped, next)

            val slotSuccess = captureSuccess(mockSource)
            val result = sut.skip(mockContext)

            result shouldBe 1

            checkCapturedTranslationKey(
                slotSuccess.captured.get(),
                "oms.command.scheduledrestart.skip.success",
                TimeFormatter.formatDateTime(skipped).literal().bold(),
                TimeFormatter.formatDateTime(next).literal().bold()
            )
        }

        should("send failure message when skip returns AlreadySkipped") {
            every { mockFeature.isEnabled() } returns true

            val next = ZonedDateTime.now().plusHours(2).toEpochSecond()
            every { mockFeature.skip() } returns SkipResult.AlreadySkipped(next)

            val slotFail = captureFail(mockSource)
            val result = sut.skip(mockContext)

            result shouldBe 1

            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.scheduledrestart.skip.already_skipped",
                TimeFormatter.formatDateTime(next).literal().bold()
            )
        }
    }
})
