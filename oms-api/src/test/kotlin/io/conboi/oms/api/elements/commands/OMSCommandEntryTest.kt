package io.conboi.oms.api.elements.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import net.minecraft.commands.CommandSourceStack

class OMSCommandEntryTest : FunSpec({

    lateinit var baseBuilder: ArgumentBuilder<CommandSourceStack, *>
    lateinit var childBuilder: ArgumentBuilder<CommandSourceStack, *>

    beforeTest {
        baseBuilder = mockk(relaxed = true)
        childBuilder = mockk(relaxed = true)
    }

    test("should build base command and append additional commands") {
        val childEntry = object : OMSCommandEntry() {
            override fun init(): ArgumentBuilder<CommandSourceStack, *> = childBuilder
        }

        val entry = object : OMSCommandEntry() {
            override fun init(): ArgumentBuilder<CommandSourceStack, *> = baseBuilder
            override fun additionalCommands(): List<OMSCommandEntry> = listOf(childEntry)
        }

        entry.build()

        verify { baseBuilder.then(childBuilder) }
    }

    test("should build base command without additional commands") {
        val entry = object : OMSCommandEntry() {
            override fun init(): ArgumentBuilder<CommandSourceStack, *> = baseBuilder
        }

        val result = entry.build()

        result shouldBe baseBuilder
        verify(exactly = 0) { baseBuilder.then(any<ArgumentBuilder<CommandSourceStack, *>>()) }
    }
})