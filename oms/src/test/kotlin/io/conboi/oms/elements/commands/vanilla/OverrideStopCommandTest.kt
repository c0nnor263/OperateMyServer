package io.conboi.oms.elements.commands.vanilla

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import io.conboi.oms.content.StopManager
import io.conboi.oms.core.foundation.reason.RegularStop
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import java.util.function.Supplier
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

class OverrideStopCommandTest : FunSpec({

    test("register should replace stop command and execute logic") {
        val dispatcher = CommandDispatcher<CommandSourceStack>()

        val originalStopNode = LiteralCommandNode<CommandSourceStack>(
            "stop",
            { 0 },
            { true },
            null,
            null,
            false
        )
        dispatcher.root.addChild(originalStopNode)

        dispatcher.root.children.map { it.name } shouldContain "stop"

        mockkObject(StopManager)
        every { StopManager.writeReason(any()) } just Runs

        val command = OverrideStopCommand()
        command.register(dispatcher)

        dispatcher.root.children.map { it.name } shouldContain "stop"

        val source = mockk<CommandSourceStack>(relaxed = true)
        val server = mockk<MinecraftServer>(relaxed = true)
        every { source.hasPermission(4) } returns true
        every { source.server } returns server

        val result = dispatcher.execute("stop", source)

        result shouldBe 1
        verify { StopManager.writeReason(RegularStop) }

        val supplierSlot = slot<Supplier<Component>>()
        verify { source.sendSuccess(capture(supplierSlot), eq(true)) }
        supplierSlot.captured.get().string shouldBe "Stopping the server"

        verify { server.halt(false) }

        unmockkObject(StopManager)
    }
})