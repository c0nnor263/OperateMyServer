package io.conboi.oms.elements.commands

import com.mojang.brigadier.CommandDispatcher
import io.conboi.oms.elements.commands.feature.FeatureCommand
import io.conboi.oms.elements.commands.utilities.RestartCommand
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class OperateMyServerCommandBranchTest : ShouldSpec({
    lateinit var sut: OperateMyServerCommandBranch

    val mockDispatcher = mockk<CommandDispatcher<CommandSourceStack>>(relaxed = true)

    beforeEach {
        mockkConstructor(RestartCommand::class)
        mockkConstructor(FeatureCommand::class)

        sut = OperateMyServerCommandBranch()
    }

    afterEach {
        clearAllMocks()
    }

    should("call build on RestartCommand and FeatureCommand when registering branch") {
        val restartLiteral = Commands.literal("restart")
        val featureLiteral = Commands.literal("feature")

        every { anyConstructed<RestartCommand>().build() } returns restartLiteral
        every { anyConstructed<FeatureCommand>().build() } returns featureLiteral

        sut.register(mockDispatcher)

        verify(exactly = 1) { anyConstructed<RestartCommand>().build() }
        verify(exactly = 1) { anyConstructed<FeatureCommand>().build() }
    }
})
