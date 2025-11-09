package io.conboi.oms.event

import com.mojang.brigadier.CommandDispatcher
import io.conboi.oms.elements.commands.OperateMyServerCommandBranch
import io.conboi.oms.elements.commands.vanilla.OverrideStopCommand
import io.kotest.core.spec.style.FunSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import io.mockk.verifyOrder
import net.minecraft.commands.CommandSourceStack
import net.minecraftforge.event.RegisterCommandsEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class CommandEventsTest : FunSpec({

    test("onRegisterCommands should post lifecycle event and register both command branches in order") {
        mockkConstructor(OperateMyServerCommandBranch::class)
        mockkConstructor(OverrideStopCommand::class)
        mockkObject(FORGE_BUS)

        val dispatcher = mockk<CommandDispatcher<CommandSourceStack>>(relaxed = true)
        val event = mockk<RegisterCommandsEvent>()
        every { event.dispatcher } returns dispatcher

        every { anyConstructed<OperateMyServerCommandBranch>().register(dispatcher) } just Runs
        every { anyConstructed<OverrideStopCommand>().register(dispatcher) } just Runs
        every { FORGE_BUS.post(any()) } returns true

        try {
            CommandEvents.onRegisterCommands(event)

            verifyOrder {
                FORGE_BUS.post(match { it is OMSLifecycleInternal.Feature.RegisterEvent })
                anyConstructed<OperateMyServerCommandBranch>().register(dispatcher)
                anyConstructed<OverrideStopCommand>().register(dispatcher)
            }
        } finally {
            unmockkConstructor(OperateMyServerCommandBranch::class)
            unmockkConstructor(OverrideStopCommand::class)
            unmockkObject(FORGE_BUS)
        }
    }
})