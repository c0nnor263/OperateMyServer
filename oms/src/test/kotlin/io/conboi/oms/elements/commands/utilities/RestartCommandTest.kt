package io.conboi.oms.elements.commands.utilities

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.common.foundation.reason.ManualRestartStop
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class RestartCommandTest : ShouldSpec({

    lateinit var sut: RestartCommand
    val mockServer = mockk<MinecraftServer>(relaxed = true)
    val mockSource = mockk<CommandSourceStack>(relaxed = true)

    beforeSpec {
        mockkObject(FORGE_BUS)
    }

    beforeEach {
        every { FORGE_BUS.post(any()) } returns true

        every { mockSource.server } returns mockServer

        sut = RestartCommand()
    }

    afterEach {
        clearAllMocks()
    }

    should("post StopRequestedEvent with ManualRestartStop on restart command") {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        dispatcher.register(sut.build() as LiteralArgumentBuilder<CommandSourceStack>)


        val result = dispatcher.execute("restart", mockSource)

        result shouldBe 1

        val slot = slot<OMSActions.StopRequestedEvent>()
        verify { FORGE_BUS.post(capture(slot)) }

        slot.captured.server shouldBe mockServer
        slot.captured.reason shouldBe ManualRestartStop
    }
})
