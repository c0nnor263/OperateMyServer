package io.conboi.oms.event.listener

import com.mojang.brigadier.CommandDispatcher
import io.conboi.oms.event.OMSInternal
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import net.minecraft.commands.CommandSourceStack
import net.minecraftforge.event.RegisterCommandsEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class CommandEventsTest : ShouldSpec({

    val mockDispatcher = mockk<CommandDispatcher<CommandSourceStack>>(relaxed = true)
    val mockRegisterCommandsEvent = mockk<RegisterCommandsEvent>()

    beforeSpec {
        mockkObject(FORGE_BUS)
    }

    beforeEach {
        every { FORGE_BUS.post(any()) } returns true

        every { mockRegisterCommandsEvent.dispatcher } returns mockDispatcher
    }

    should("post RegisterEvent and register commands on RegisterCommandsEvent") {
        CommandsListener.onRegisterCommands(mockRegisterCommandsEvent)
        verify(exactly = 1) { FORGE_BUS.post(any<OMSInternal.Addon.PrepareEvent>()) }
    }
})
