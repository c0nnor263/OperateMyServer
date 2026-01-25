package io.conboi.oms.api.elements.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.minecraft.commands.CommandSourceStack

class OMSCommandBranchTest : ShouldSpec({

    val mockDispatcher: CommandDispatcher<CommandSourceStack> = mockk(relaxed = true)
    val mockGroupBuilder: LiteralArgumentBuilder<CommandSourceStack> = mockk(relaxed = true)
    val mockCommandEntry: OMSCommandEntry = mockk(relaxed = true)
    val mockCommandBuilder: LiteralArgumentBuilder<CommandSourceStack> = mockk(relaxed = true)

    beforeEach {
        every { mockCommandEntry.build() } returns mockCommandBuilder
    }

    should("register commands when groupBuilder is provided") {
        val branch = object : OMSCommandBranch() {
            override fun getCommands(): List<OMSCommandEntry> = listOf(mockCommandEntry)
        }

        branch.register(mockDispatcher, mockGroupBuilder)

        verify { mockCommandEntry.build() }
        verify { mockGroupBuilder.then(mockCommandBuilder) }
        verify { mockDispatcher.register(mockGroupBuilder) }
    }

    should("not register commands when groupBuilder is null") {
        val branch = object : OMSCommandBranch() {
            override fun getCommands(): List<OMSCommandEntry> = listOf(mockCommandEntry)
        }

        branch.register(mockDispatcher)

        verify(exactly = 0) { mockCommandEntry.build() }
        verify(exactly = 0) { mockDispatcher.register(any()) }
        verify(exactly = 0) { mockGroupBuilder.then(any<LiteralArgumentBuilder<CommandSourceStack>>()) }
    }
})
