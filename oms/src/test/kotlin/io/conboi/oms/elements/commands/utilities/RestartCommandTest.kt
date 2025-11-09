package io.conboi.oms.elements.commands.utilities

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.core.foundation.reason.ManualRestartStop
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class RestartCommandTest : FunSpec({

    test("restart command should post StopRequestedEvent with ManualRestartStop") {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val restartCommand = RestartCommand()
        dispatcher.register(restartCommand.build() as LiteralArgumentBuilder<CommandSourceStack>)

        val server = mockk<MinecraftServer>(relaxed = true)
        val source = mockk<CommandSourceStack>(relaxed = true) {
            every { this@mockk.server } returns server
        }

        mockkObject(FORGE_BUS)
        every { FORGE_BUS.post(any()) } returns true

        val result = dispatcher.execute("restart", source)

        result shouldBe 1

        val eventSlot = slot<OMSLifecycle.StopRequestedEvent>()
        verify { FORGE_BUS.post(capture(eventSlot)) }

        eventSlot.captured.server shouldBe server
        eventSlot.captured.reason shouldBe ManualRestartStop

        unmockkObject(FORGE_BUS)
    }
})