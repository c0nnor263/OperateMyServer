package io.conboi.oms.elements.commands.feature.available

import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.OmsAddons
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.manager.FeatureManager
import io.conboi.oms.common.text.ComponentStyles.color
import io.conboi.oms.common.text.ComponentStyles.literal
import io.conboi.oms.foundation.addon.AddonInstance
import io.conboi.oms.testing.captureFail
import io.conboi.oms.testing.captureSuccess
import io.conboi.oms.testing.checkCapturedTranslationKey
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack

class FeatureAvailableCommonTest : ShouldSpec({

    lateinit var sut: FeatureAvailableCommon

    val mockFeature = mockk<OmsFeature<*>>(relaxed = true)
    val mockSource = mockk<CommandSourceStack>(relaxed = true)

    val mockFeatureManager = mockk<FeatureManager>(relaxed = true)
    val mockAddonContext = mockk<AddonContext>(relaxed = true)
    val mockAddonInstance = mockk<AddonInstance>(relaxed = true)

    beforeSpec {
        mockkObject(OmsAddons)
    }

    beforeEach {
        every { mockFeatureManager.getFeatureById<OmsFeature<*>>("abc") } returns mockFeature
        every { mockAddonContext.featureManager } returns mockFeatureManager
        every { mockAddonInstance.context } returns mockAddonContext
        every { OmsAddons.get("m") } returns mockAddonInstance

        sut = FeatureAvailableCommon()
    }

    afterEach {
        clearAllMocks()
    }

    fun createMockContext(
        fullName: String,
        action: String,
    ): CommandContext<CommandSourceStack> {
        return mockk {
            every { source } returns mockSource
            every { nodes } returns listOf(
                mockk { every { node.name } returns "feature" },
                mockk { every { node.name } returns fullName },
                mockk { every { node.name } returns action },
            )
        }
    }

    context("enable") {

        should("enable feature successfully") {
            val ctx = createMockContext("m:abc", "enable")
            every { mockFeature.isEnabled() } returns false

            val slotSuccess = captureSuccess(mockSource)

            val result = sut.execute(ctx, available = true)

            result shouldBe 1
            verify { mockFeature.enable() }

            checkCapturedTranslationKey(
                slotSuccess.captured.get(),
                "oms.command.feature.enabled",
                "m:abc".literal().color(ChatFormatting.GREEN)
            )
        }

        should("report already enabled feature") {
            val ctx = createMockContext("m:abc", "enable")
            every { mockFeature.isEnabled() } returns true

            val slotFail = captureFail(mockSource)

            val result = sut.execute(ctx, available = true)

            result shouldBe 1
            verify(exactly = 0) { mockFeature.enable() }

            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.feature.already_enabled",
                "m:abc"
            )
        }
    }

    context("disable") {

        should("disable feature successfully") {
            val ctx = createMockContext("m:abc", "disable")
            every { mockFeature.isEnabled() } returns true

            val slotSuccess = captureSuccess(mockSource)

            val result = sut.execute(ctx, available = false)

            result shouldBe 1
            verify { mockFeature.disable() }

            checkCapturedTranslationKey(
                slotSuccess.captured.get(),
                "oms.command.feature.disabled",
                "m:abc".literal().color(ChatFormatting.GREEN)
            )
        }

        should("report already disabled feature") {
            val ctx = createMockContext("m:abc", "disable")
            every { mockFeature.isEnabled() } returns false

            val slotFail = captureFail(mockSource)
            val result = sut.execute(ctx, available = false)

            result shouldBe 1
            verify(exactly = 0) { mockFeature.disable() }

            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.feature.already_disabled",
                "m:abc"
            )
        }
    }

    context("search") {

        should("fail when addon does not exist") {
            val ctx = createMockContext("m:abc", "enable")
            every { OmsAddons.get("m") } returns null

            val slotFail = captureFail(mockSource)

            val result = sut.execute(ctx, available = true)

            result shouldBe 0

            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.feature.not_found",
                "m:abc"
            )
        }

        should("fail when addon exists but feature not found") {
            val ctx = createMockContext("m:abc", "enable")
            every { mockFeatureManager.getFeatureById<OmsFeature<*>>("abc") } returns null

            val slotFail = captureFail(mockSource)

            val result = sut.execute(ctx, available = true)

            result shouldBe 0

            checkCapturedTranslationKey(
                slotFail.captured,
                "oms.command.feature.not_found",
                "m:abc"
            )
        }
    }
})
