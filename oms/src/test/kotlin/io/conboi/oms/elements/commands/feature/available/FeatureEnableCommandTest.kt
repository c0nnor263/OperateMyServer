package io.conboi.oms.elements.commands.feature.available

import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.OmsAddons
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.feature.Priority
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
import net.minecraft.commands.CommandSourceStack

class FeatureEnableCommandTest : ShouldSpec({

    lateinit var sut: FeatureEnableCommand
    val mockAddon = mockk<OmsAddon>(relaxed = true)
    val mockFeature = mockk<OmsFeature<*>>(relaxed = true)
    val mockSource = mockk<CommandSourceStack>(relaxed = true)

    beforeSpec {
        mockkObject(OmsAddons)
    }

    beforeEach {
        every { mockFeature.info() } returns FeatureInfo(
            id = "abc",
            priority = Priority.NONE
        )

        every { mockAddon.getFeatureById<OmsFeature<*>>("abc") } returns mockFeature
        every { OmsAddons.get("m") } returns mockAddon

        sut = FeatureEnableCommand()
    }

    afterEach {
        clearAllMocks()
    }

    fun createMockContext(fullName: String): CommandContext<CommandSourceStack> {
        return mockk {
            every { source } returns mockSource
            every { nodes } returns listOf(
                mockk { every { node.name } returns "feature" },
                mockk { every { node.name } returns fullName },
                mockk { every { node.name } returns "enable" }
            )
        }
    }

    should("fail when addon not found") {
        val ctx = createMockContext("m:abc")

        every { OmsAddons.get("m") } returns null

        val slotFail = captureFail(mockSource)

        val result = sut.build().command.run(ctx)

        result shouldBe 0
        checkCapturedTranslationKey(
            slotFail.captured,
            "oms.command.feature.not_found",
            "m:abc"
        )
    }

    should("fail when addon exists but does not contain feature") {
        val ctx = createMockContext("m:abc")

        every { mockAddon.getFeatureById<OmsFeature<*>>("abc") } returns null

        val slotFail = captureFail(mockSource)

        val result = sut.build().command.run(ctx)

        result shouldBe 0
        checkCapturedTranslationKey(
            slotFail.captured,
            "oms.command.feature.not_found",
            "m:abc"
        )
    }

    should("report already enabled feature") {
        val ctx = createMockContext("m:abc")

        every { mockFeature.isEnabled() } returns true

        val slotFail = captureFail(mockSource)

        val result = sut.build().command.run(ctx)

        result shouldBe 1
        verify(exactly = 0) { mockFeature.enable() }

        checkCapturedTranslationKey(
            slotFail.captured,
            "oms.command.feature.already_enabled",
            "m:abc"
        )
    }

    should("enable feature successfully") {
        val ctx = createMockContext("m:abc")

        every { mockFeature.isEnabled() } returns false

        val slotSuccess = captureSuccess(mockSource)

        val result = sut.build().command.run(ctx)

        result shouldBe 1
        verify { mockFeature.enable() }

        checkCapturedTranslationKey(
            slotSuccess.captured.get(),
            "oms.command.feature.enabled",
            "m:abc"
        )
    }
})
