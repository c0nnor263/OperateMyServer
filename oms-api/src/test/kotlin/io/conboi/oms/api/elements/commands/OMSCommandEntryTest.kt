package io.conboi.oms.api.elements.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import net.minecraft.commands.CommandSourceStack

class OMSCommandEntryTest : ShouldSpec({

    val mockBaseBuilder: ArgumentBuilder<CommandSourceStack, *> = mockk(relaxed = true)
    val mockChildBuilder: ArgumentBuilder<CommandSourceStack, *> = mockk(relaxed = true)

    should("append additional command builders") {
        val childEntry = object : OMSCommandEntry() {
            override fun init(): ArgumentBuilder<CommandSourceStack, *> = mockChildBuilder
        }

        val entry = object : OMSCommandEntry() {
            override fun init(): ArgumentBuilder<CommandSourceStack, *> = mockBaseBuilder
            override fun additionalCommands(): List<OMSCommandEntry> = listOf(childEntry)
        }

        entry.build()

        verify { mockBaseBuilder.then(mockChildBuilder) }
    }

    should("build base command when no additional commands exist") {
        val entry = object : OMSCommandEntry() {
            override fun init(): ArgumentBuilder<CommandSourceStack, *> = mockBaseBuilder
        }

        val result = entry.build()

        result shouldBe mockBaseBuilder
        verify(exactly = 0) { mockBaseBuilder.then(any<LiteralArgumentBuilder<CommandSourceStack>>()) }
    }
})
