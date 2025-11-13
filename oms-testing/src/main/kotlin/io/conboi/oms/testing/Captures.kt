package io.conboi.oms.testing

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.slot
import java.util.function.Supplier
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

/**
 * captureFail(mockSource) -> CapturingSlot<Component>
 */
fun captureFail(source: CommandSourceStack): CapturingSlot<Component> {
    val s = slot<Component>()
    every { source.sendFailure(capture(s)) } returns Unit
    return s
}

/**
 * captureSuccess(mockSource) -> CapturingSlot<Component>
 */
fun captureSuccess(source: CommandSourceStack): CapturingSlot<Supplier<Component>> {
    val s = slot<Supplier<Component>>()
    every {
        source.sendSuccess(capture(s), any())
    } returns Unit
    return s
}