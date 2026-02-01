package io.conboi.oms.event.listener

import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.content.StopManager
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class OMSActionsListenerTest : ShouldSpec({
    val mockServer = mockk<MinecraftServer>(relaxed = true)
    val mockReason = mockk<StopReason>()

    beforeSpec {
        mockkObject(FORGE_BUS)
        mockkObject(StopManager)
    }

    beforeEach {
        every { FORGE_BUS.post(any()) } returns true
        every { StopManager.installHook() } just Runs
        every { StopManager.stop(any()) } just Runs
        every { mockReason.name } returns "test_reason"
        every { mockReason.messageId } returns "oms.stop_reason.test_reason"
    }

    afterSpec {
        clearAllMocks()
    }

    context("onStopRequestedEvent") {
        should("call StopManager.stop with the event") {
            val event = OMSActions.StopRequestedEvent(mockServer, mockReason)
            OMSActionsListener.onStopRequestedEvent(event)

            verify {
                StopManager.stop(event)
            }
        }
    }
})
