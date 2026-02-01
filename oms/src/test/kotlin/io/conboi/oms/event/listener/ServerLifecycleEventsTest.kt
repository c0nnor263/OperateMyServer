package io.conboi.oms.event.listener

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.content.StopManager
import io.conboi.oms.event.OMSInternal
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import net.minecraft.server.MinecraftServer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class ServerLifecycleEventsTest : ShouldSpec({

    val mockServer = mockk<MinecraftServer>(relaxed = true)

    beforeSpec {
        mockkObject(FORGE_BUS)
        mockkObject(StopManager)
    }

    beforeEach {
        every { FORGE_BUS.post(any()) } returns true
    }

    afterEach {
        clearAllMocks()
    }


    context("onServerStarted") {
        should("post ReadyEvent when server starts") {
            val event = ServerStartedEvent(mockServer)

            ServerLifecycleListener.onServerStarted(event)

            verify {
                FORGE_BUS.post(match { posted ->
                    posted is OMSInternal.Server.ReadyEvent &&
                            posted.server == mockServer
                })
            }
        }
    }

    context("onServerTick") {
        should("post TickingEvent only on END phase") {
            every { StopManager.isServerStopping() } returns false

            val startEvent = TickEvent.ServerTickEvent(
                TickEvent.Phase.START,
                { true },
                mockServer
            )
            ServerLifecycleListener.onServerTick(startEvent)

            verify(exactly = 0) { FORGE_BUS.post(ofType<OMSLifecycle.TickingEvent>()) }

            val endEvent = TickEvent.ServerTickEvent(
                TickEvent.Phase.END,
                { true },
                mockServer
            )
            ServerLifecycleListener.onServerTick(endEvent)

            verify {
                FORGE_BUS.post(match { posted ->
                    posted is OMSLifecycle.TickingEvent &&
                            posted.server == mockServer && !posted.isServerStopping
                })
            }
        }
    }

    context("onServerStopping") {
        should("post PreShutdownEvent when server stops") {
            val event = ServerStoppingEvent(mockServer)

            ServerLifecycleListener.onServerStopping(event)

            verify {
                FORGE_BUS.post(match { posted ->
                    posted is OMSInternal.Server.PreShutdownEvent &&
                            posted.server == mockServer
                })
            }

        }
    }
})
