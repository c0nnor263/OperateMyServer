package io.conboi.oms.elements.commands.feature

import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.FeatureManager
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import net.minecraft.commands.CommandSourceStack

class FeatureEnableCommandTest : FunSpec({

    test("should fail if feature not found") {
        mockkObject(OMSFeatureManagers)

        val fakeManager = mockk<FeatureManager>()
        every { fakeManager.getFeatureById<OmsFeature<*>>("my_feature") } returns null
        every { OMSFeatureManagers.oms } returns fakeManager

        val source = mockk<CommandSourceStack>(relaxed = true)
        val context = mockk<CommandContext<CommandSourceStack>> {
            every { this@mockk.source } returns source
            every { nodes } returns listOf(
                mockk { every { node.name } returns "feature" },
                mockk { every { node.name } returns "my_feature" },
                mockk { every { node.name } returns "enable" }
            )
        }

        val command = FeatureEnableCommand()
        val result = command.javaClass
            .getDeclaredMethod("enableFeature", CommandContext::class.java)
            .apply { isAccessible = true }
            .invoke(command, context) as Int

        result shouldBe 0
        verify { source.sendFailure(any()) }

        unmockkObject(OMSFeatureManagers)
    }

    test("should report already enabled feature") {
        mockkObject(OMSFeatureManagers)

        val feature = mockk<OmsFeature<*>>(relaxed = true)
        every { feature.isEnabled() } returns true
        every { feature.info } returns FeatureInfo("mod:my_feature", FeatureInfo.Priority.NONE)
        every { OMSFeatureManagers.oms.getFeatureById<OmsFeature<*>>("my_feature") } returns feature

        val source = mockk<CommandSourceStack>(relaxed = true)
        val context = mockk<CommandContext<CommandSourceStack>> {
            every { this@mockk.source } returns source
            every { nodes } returns listOf(
                mockk { every { node.name } returns "feature" },
                mockk { every { node.name } returns "my_feature" },
                mockk { every { node.name } returns "enable" }
            )
        }

        val command = FeatureEnableCommand()
        val result = command.javaClass
            .getDeclaredMethod("enableFeature", CommandContext::class.java)
            .apply { isAccessible = true }
            .invoke(command, context) as Int

        result shouldBe 1
        verify { source.sendSuccess(any(), false) }
        verify(exactly = 0) { feature.enable() }

        unmockkObject(OMSFeatureManagers)
    }

    test("should enable feature successfully") {
        mockkObject(OMSFeatureManagers)

        val feature = mockk<OmsFeature<*>>(relaxed = true)
        every { feature.isEnabled() } returns false
        every { feature.info } returns FeatureInfo("mod:my_feature", FeatureInfo.Priority.NONE)
        every { OMSFeatureManagers.oms.getFeatureById<OmsFeature<*>>("my_feature") } returns feature

        val source = mockk<CommandSourceStack>(relaxed = true)
        val context = mockk<CommandContext<CommandSourceStack>> {
            every { this@mockk.source } returns source
            every { nodes } returns listOf(
                mockk { every { node.name } returns "feature" },
                mockk { every { node.name } returns "my_feature" },
                mockk { every { node.name } returns "enable" }
            )
        }

        val command = FeatureEnableCommand()
        val result = command.javaClass
            .getDeclaredMethod("enableFeature", CommandContext::class.java)
            .apply { isAccessible = true }
            .invoke(command, context) as Int

        result shouldBe 1
        verify { feature.enable() }
        verify { source.sendSuccess(any(), true) }

        unmockkObject(OMSFeatureManagers)
    }
})