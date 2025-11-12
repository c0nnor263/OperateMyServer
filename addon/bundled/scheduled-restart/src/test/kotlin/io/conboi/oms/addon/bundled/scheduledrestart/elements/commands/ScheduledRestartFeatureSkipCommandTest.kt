package io.conboi.oms.addon.bundled.scheduledrestart.elements.commands

import com.mojang.brigadier.context.CommandContext
import io.conboi.oms.addon.bundled.scheduledrestart.ScheduledRestartFeature
import io.conboi.oms.addon.bundled.scheduledrestart.content.SkipResult
import io.conboi.oms.addon.bundled.scheduledrestart.infrastructure.config.CScheduledRestartFeature
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.foundation.feature.FeatureManager
import io.conboi.oms.utils.foundation.TimeFormatter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import java.time.ZonedDateTime
import java.util.function.Supplier
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.TranslatableContents

class ScheduledRestartFeatureSkipCommandTest : FunSpec({

    test("should return 0 if feature not found") {
        mockkObject(OMSFeatureManagers)
        every { OMSFeatureManagers.oms.getFeatureById<ScheduledRestartFeature>(any()) } returns null

        val source = mockk<CommandSourceStack>(relaxed = true)
        val context = mockk<CommandContext<CommandSourceStack>> {
            every { this@mockk.source } returns source
        }

        val command = ScheduledRestartFeatureSkipCommand()
        val result = command.javaClass.getDeclaredMethod("skip", CommandContext::class.java)
            .apply { isAccessible = true }
            .invoke(command, context) as Int

        result shouldBe 0
        verify { source.sendFailure(any()) }

        unmockkObject(OMSFeatureManagers)
    }

    test("should return 0 if feature is not enabled") {
        val feature = mockk<ScheduledRestartFeature>()
        every { feature.isEnabled() } returns false
        every { feature.info.id } returns CScheduledRestartFeature.NAME

        mockkObject(OMSFeatureManagers)
        every { OMSFeatureManagers.oms.getFeatureById<ScheduledRestartFeature>(any()) } returns feature

        val source = mockk<CommandSourceStack>(relaxed = true)
        val context = mockk<CommandContext<CommandSourceStack>> {
            every { this@mockk.source } returns source
        }

        val command = ScheduledRestartFeatureSkipCommand()
        val result = command.javaClass.getDeclaredMethod("skip", CommandContext::class.java)
            .apply { isAccessible = true }
            .invoke(command, context) as Int

        result shouldBe 0
        verify { source.sendFailure(any()) }

        unmockkObject(OMSFeatureManagers)
    }

    test("should succeed and send skip success message with correct translation") {
        val now = ZonedDateTime.now()
        val skippedTime = now.plusHours(1)
        val nextTime = now.plusHours(3)

        val feature = mockk<ScheduledRestartFeature>()
        every { feature.isEnabled() } returns true
        every { feature.info.id } returns CScheduledRestartFeature.NAME
        every { feature.skip() } returns SkipResult.Skipped(skipped = skippedTime, next = nextTime)

        mockkObject(OMSFeatureManagers)
        every { OMSFeatureManagers.oms.getFeatureById<ScheduledRestartFeature>(any()) } returns feature

        val source = mockk<CommandSourceStack>(relaxed = true)
        val context = mockk<CommandContext<CommandSourceStack>> {
            every { this@mockk.source } returns source
        }

        val supplierSlot = slot<Supplier<Component>>()
        every { source.sendSuccess(capture(supplierSlot), true) } returns Unit

        val command = ScheduledRestartFeatureSkipCommand()
        val result = command.javaClass.getDeclaredMethod("skip", CommandContext::class.java)
            .apply { isAccessible = true }
            .invoke(command, context) as Int

        result shouldBe 1

        val component = supplierSlot.captured.get()
        val translatable = (component as MutableComponent).contents as TranslatableContents

        translatable.key shouldBe "oms.command.autorestart.skip.success"
        translatable.args[0].toString() shouldContain ":" // formatTime(skipped)
        translatable.args[1].toString() shouldContain ":" // formatTime(next)

        unmockkObject(OMSFeatureManagers)
    }

    test("should report already skipped") {
        val now = ZonedDateTime.now()
        val feature = mockk<ScheduledRestartFeature>()
        every { feature.isEnabled() } returns true
        every { feature.info.id } returns CScheduledRestartFeature.NAME
        every { feature.skip() } returns SkipResult.AlreadySkipped(next = now.plusHours(2))

        mockkObject(OMSFeatureManagers)
        every { OMSFeatureManagers.oms.getFeatureById<ScheduledRestartFeature>(any()) } returns feature

        val source = mockk<CommandSourceStack>(relaxed = true)
        val context = mockk<CommandContext<CommandSourceStack>> {
            every { this@mockk.source } returns source
        }

        val command = ScheduledRestartFeatureSkipCommand()
        val result = command.javaClass.getDeclaredMethod("skip", CommandContext::class.java)
            .apply { isAccessible = true }
            .invoke(command, context) as Int

        result shouldBe 1
        verify { source.sendFailure(any()) }

        unmockkObject(OMSFeatureManagers)
    }

    test("should use fallback id when feature is null") {
        mockkObject(OMSFeatureManagers)

        val featureManager = mockk<FeatureManager>()
        every { OMSFeatureManagers.oms } returns featureManager
        every { featureManager.getFeatureById<ScheduledRestartFeature>(CScheduledRestartFeature.NAME) } returns null

        val source = mockk<CommandSourceStack>(relaxed = true)
        val context = mockk<CommandContext<CommandSourceStack>> {
            every { this@mockk.source } returns source
        }

        val command = ScheduledRestartFeatureSkipCommand()
        val result = command.javaClass
            .getDeclaredMethod("skip", CommandContext::class.java)
            .apply { isAccessible = true }
            .invoke(command, context) as Int

        result shouldBe 0
        val failureSlot = slot<Component>()
        verify { source.sendFailure(capture(failureSlot)) }

        val mutable = failureSlot.captured as MutableComponent
        val contents = mutable.contents as TranslatableContents

        contents.key shouldBe "oms.command.feature.not_found"
        (contents.args[0] as MutableComponent).contents.let { it as TranslatableContents }.key shouldBe "auto_restart"

        unmockkObject(OMSFeatureManagers)
    }

    test("should format time for today and not today") {
        val cmd = ScheduledRestartFeatureSkipCommand()

        val today = ZonedDateTime.now()
        val notToday = today.minusDays(1)

        val resultToday = TimeFormatter.formatDateTime(today)
        val resultOtherDay = TimeFormatter.formatDateTime(notToday)

        resultToday shouldContain ":" // HH:mm
        resultOtherDay shouldContain "." // dd.MM.HH:mm
    }
})