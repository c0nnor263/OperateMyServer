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

class FeatureDisableCommandTest : ShouldSpec({

    lateinit var sut: FeatureDisableCommand
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

        sut = FeatureDisableCommand()
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
                mockk { every { node.name } returns "disable" }
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

    should("fail when feature not found in addon") {
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

    should("report already disabled feature") {
        val ctx = createMockContext("m:abc")

        every { mockFeature.isEnabled() } returns false

        val slotFail = captureFail(mockSource)

        val result = sut.build().command.run(ctx)

        result shouldBe 1
        verify(exactly = 0) { mockFeature.disable() }

        checkCapturedTranslationKey(
            slotFail.captured,
            "oms.command.feature.already_disabled",
            "m:abc"
        )
    }

    should("disable feature successfully") {
        val ctx = createMockContext("m:abc")

        every { mockFeature.isEnabled() } returns true

        val slotSuccess = captureSuccess(mockSource)

        val result = sut.build().command.run(ctx)

        result shouldBe 1
        verify { mockFeature.disable() }

        checkCapturedTranslationKey(
            slotSuccess.captured.get(),
            "oms.command.feature.disabled",
            "m:abc"
        )
    }
})
