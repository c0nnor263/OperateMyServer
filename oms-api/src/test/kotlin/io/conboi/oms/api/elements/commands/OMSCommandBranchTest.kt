package io.conboi.oms.api.elements.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.minecraft.commands.CommandSourceStack

class OMSCommandBranchTest : FunSpec({

    lateinit var dispatcher: CommandDispatcher<CommandSourceStack>
    lateinit var groupBuilder: LiteralArgumentBuilder<CommandSourceStack>
    lateinit var commandEntry: OMSCommandEntry
    lateinit var commandBuilder: LiteralArgumentBuilder<CommandSourceStack>

    beforeTest {
        dispatcher = mockk(relaxed = true)
        groupBuilder = mockk(relaxed = true)
        commandBuilder = mockk(relaxed = true)

        commandEntry = mockk {
            every { build() } returns commandBuilder
        }
    }

    test("should register commands to dispatcher through groupBuilder") {
        val branch = object : OMSCommandBranch() {
            override fun getCommands(): List<OMSCommandEntry> = listOf(commandEntry)
        }

        branch.register(dispatcher, groupBuilder)

        verify { commandEntry.build() }
        verify { groupBuilder.then(commandBuilder) }
        verify { dispatcher.register(groupBuilder) }
    }

    test("should do nothing if groupBuilder is null") {
        val branch = object : OMSCommandBranch() {
            override fun getCommands(): List<OMSCommandEntry> = listOf(commandEntry)
        }

        // nothing should be called
        branch.register(dispatcher, null)

        verify(exactly = 0) { commandEntry.build() }
        verify(exactly = 0) { dispatcher.register(any()) }
        verify(exactly = 0) { groupBuilder.then(any<LiteralArgumentBuilder<CommandSourceStack>>()) }
    }
})