package io.conboi.oms.elements.commands.vanilla

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import io.conboi.oms.common.foundation.reason.RegularStop
import io.conboi.oms.content.StopManager
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer

class OverrideStopCommandTest : ShouldSpec({

    lateinit var sut: OverrideStopCommand
    val mockSource = mockk<CommandSourceStack>(relaxed = true)
    val mockServer = mockk<MinecraftServer>(relaxed = true)

    beforeEach {
        mockkObject(StopManager)
        every { StopManager.writeReason(any()) } just Runs

        every { mockSource.hasPermission(4) } returns true
        every { mockSource.server } returns mockServer

        sut = OverrideStopCommand()
    }

    afterEach {
        clearAllMocks()
    }

    should("replace stop command and execute overridden logic") {
        val dispatcher = CommandDispatcher<CommandSourceStack>()

        val original = LiteralCommandNode<CommandSourceStack>(
            "stop",
            { 0 },
            { true },
            null,
            null,
            false
        )
        dispatcher.root.addChild(original)

        dispatcher.root.children.map { it.name } shouldContain "stop"

        sut.register(dispatcher)

        dispatcher.root.children.map { it.name } shouldContain "stop"


        val result = dispatcher.execute("stop", mockSource)

        result shouldBe 1
        verify { StopManager.writeReason(RegularStop) }
        verify { mockServer.halt(false) }
    }
})
