package io.conboi.oms.elements.commands

import com.mojang.brigadier.CommandDispatcher
import io.conboi.oms.elements.commands.feature.FeatureCommand
import io.conboi.oms.elements.commands.utilities.RestartCommand
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class OperateMyServerCommandBranchTest : FunSpec({

    test("register should call build() on all subcommands") {
        val dispatcher = mockk<CommandDispatcher<CommandSourceStack>>(relaxed = true)

        mockkConstructor(RestartCommand::class)
        mockkConstructor(FeatureCommand::class)

        val restartLiteral = Commands.literal("restart")
        val featureLiteral = Commands.literal("feature")

        every { anyConstructed<RestartCommand>().build() } returns restartLiteral
        every { anyConstructed<FeatureCommand>().build() } returns featureLiteral

        OperateMyServerCommandBranch().register(dispatcher)

        verify {
            anyConstructed<RestartCommand>().build()
            anyConstructed<FeatureCommand>().build()
        }

        unmockkConstructor(RestartCommand::class)
        unmockkConstructor(FeatureCommand::class)
    }
})