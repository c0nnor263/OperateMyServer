package io.conboi.oms.addon.bundled.scheduledrestart.elements.commands

import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.addon.bundled.scheduledrestart.ScheduledRestartFeature
import io.conboi.oms.addon.bundled.scheduledrestart.content.SkipResult
import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.api.OmsAddons
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.core.OperateMyServer
import io.conboi.oms.testing.captureFail
import io.conboi.oms.testing.captureSuccess
import io.conboi.oms.testing.checkCapturedTranslationKey
import io.conboi.oms.utils.foundation.TimeFormatter
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import java.time.ZonedDateTime
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

class ScheduledRestartFeatureSkipCommandTest : ShouldSpec({

    lateinit var sut: ScheduledRestartFeatureSkipCommand

    val mockContext = mockk<CommandContext<CommandSourceStack>>(relaxed = true)
    val mockSource = mockk<CommandSourceStack>(relaxed = true)
    val mockAddon = mockk<OmsAddon>()
    val mockFeature = mockk<ScheduledRestartFeature>()
    val mockFeatureInfo = mockk<FeatureInfo>()

    beforeSpec {
        mockkObject(OmsAddons)
    }

    beforeEach {
        every { mockContext.source } returns mockSource
        every { mockFeatureInfo.id } returns CScheduledRestartFeature.NAME
        every { mockFeatureInfo.priority } returns Priority.COMMON
        every { mockFeature.info() } returns mockFeatureInfo

        sut = ScheduledRestartFeatureSkipCommand()
    }

    afterEach {
        clearAllMocks()
    }

    context("0") {

        should("return 0 and send not_found when addon is null") {
            every { OmsAddons.get(OperateMyServer.MOD_ID) } returns null

            val slotFail = captureFail(mockSource)
            val result = sut.skip(mockContext)

            result shouldBe 0
            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.feature.not_found",
                Component.translatable(CScheduledRestartFeature.NAME)
            )
        }

        should("return 0 and send not_found when feature is null") {
            every { OmsAddons.get(OperateMyServer.MOD_ID) } returns mockAddon
            every { mockAddon.getFeatureById<ScheduledRestartFeature>(any()) } returns null

            val slotFail = captureFail(mockSource)
            val result = sut.skip(mockContext)

            result shouldBe 0
            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.feature.not_found",
                Component.translatable(CScheduledRestartFeature.NAME)
            )
        }

        should("return 0 and send not_enabled when feature exists but disabled") {
            every { OmsAddons.get(OperateMyServer.MOD_ID) } returns mockAddon
            every { mockAddon.getFeatureById<ScheduledRestartFeature>(any()) } returns mockFeature
            every { mockFeature.isEnabled() } returns false

            val slotFail = captureFail(mockSource)
            val result = sut.skip(mockContext)

            result shouldBe 0
            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.feature.not_enabled",
                Component.translatable(CScheduledRestartFeature.NAME)
            )
        }
    }

    context("Command.SINGLE_SUCCESS") {

        should("send success message when skip returns Skipped") {
            every { OmsAddons.get(OperateMyServer.MOD_ID) } returns mockAddon
            every { mockAddon.getFeatureById<ScheduledRestartFeature>(any()) } returns mockFeature
            every { mockFeature.isEnabled() } returns true

            val now = ZonedDateTime.now()
            val skipTime = now.plusHours(1).toEpochSecond()
            val nextTime = now.plusHours(3).toEpochSecond()

            every { mockFeature.skip() } returns SkipResult.Skipped(skipTime, nextTime)

            val slotSuccess = captureSuccess(mockSource)

            val result = sut.skip(mockContext)

            result shouldBe 1

            checkCapturedTranslationKey(
                slotSuccess.captured.get(),
                "oms.command.autorestart.skip.success",
                TimeFormatter.formatDateTime(skipTime),
                TimeFormatter.formatDateTime(nextTime)
            )
        }

        should("send failure message when skip returns AlreadySkipped") {
            every { OmsAddons.get(OperateMyServer.MOD_ID) } returns mockAddon
            every { mockAddon.getFeatureById<ScheduledRestartFeature>(any()) } returns mockFeature
            every { mockFeature.isEnabled() } returns true

            val now = ZonedDateTime.now()
            val next = now.plusHours(2).toEpochSecond()

            every { mockFeature.skip() } returns SkipResult.AlreadySkipped(next)

            val slotFail = captureFail(mockSource)

            val result = sut.skip(mockContext)

            result shouldBe 1

            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.autorestart.skip.already_skipped",
                TimeFormatter.formatDateTime(next)
            )
        }
    }
})
