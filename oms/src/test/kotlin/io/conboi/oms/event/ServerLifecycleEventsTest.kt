package io.conboi.oms.event

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.content.StopManager
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import net.minecraft.server.MinecraftServer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class ServerLifecycleEventsTest : FunSpec({

    val server = mockk<MinecraftServer>(relaxed = true)

    beforeTest {
        mockkObject(FORGE_BUS)
        mockkObject(StopManager)
        every { FORGE_BUS.post(any()) } returns true
    }

    afterTest {
        unmockkAll()
    }

    test("onServerStarted should post OMSLifecycleInternal.Server.ReadyEvent") {
        val event = ServerStartedEvent(server)

        ServerLifecycleEvents.onServerStarted(event)

        verify {
            FORGE_BUS.post(match {
                it is OMSLifecycleInternal.Server.ReadyEvent && it.server == server
            })
        }
    }

    test("onServerTick should post TickingEvent only on END phase") {
        every { StopManager.isServerStopping() } returns false

        val tickEventStart = TickEvent.ServerTickEvent(
            TickEvent.Phase.START,
            { true },
            server
        )
        ServerLifecycleEvents.onServerTick(tickEventStart)

        verify(inverse = true) {
            FORGE_BUS.post(ofType<OMSLifecycle.TickingEvent>())
        }

        val tickEventEnd = TickEvent.ServerTickEvent(
            TickEvent.Phase.END,
            { true },
            server
        )
        ServerLifecycleEvents.onServerTick(tickEventEnd)

        verify {
            FORGE_BUS.post(match {
                it is OMSLifecycle.TickingEvent &&
                        it.server == server &&
                        !it.isServerStopping
            })
        }
    }

    test("onServerStopping should post OMSLifecycleInternal.Server.PreShutdownEvent") {
        val event = ServerStoppingEvent(server)

        ServerLifecycleEvents.onServerStopping(event)

        verify {
            FORGE_BUS.post(match {
                it is OMSLifecycleInternal.Server.PreShutdownEvent && it.server == server
            })
        }
    }
})