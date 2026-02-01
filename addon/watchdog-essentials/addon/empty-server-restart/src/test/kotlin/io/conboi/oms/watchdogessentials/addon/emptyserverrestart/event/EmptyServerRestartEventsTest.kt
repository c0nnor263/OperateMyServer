package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.event

import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.ServerAccess
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import net.minecraft.server.MinecraftServer
import net.minecraftforge.event.entity.player.PlayerEvent

class EmptyServerRestartEventsTest : ShouldSpec({

    val mockServer = mockk<MinecraftServer>(relaxed = true)
    val mockFeature = mockk<EmptyServerRestartFeature>(relaxed = true)

    lateinit var sut: EmptyServerRestartEvents

    beforeSpec {
        mockkObject(ServerAccess)
    }

    beforeEach {
        every { ServerAccess.getCurrentServer() } returns mockServer
        sut = EmptyServerRestartEvents(mockFeature)
    }

    afterEach {
        clearAllMocks()
    }

    should("call clearTime on player login") {
        val event = mockk<PlayerEvent.PlayerLoggedInEvent>()

        sut.onPlayerLoggedIn(event)

        verify { mockFeature.clearTime() }
    }

    should("call initTime on logout when playerCount <= 1") {
        every { mockServer.playerCount } returns 1
        val event = mockk<PlayerEvent.PlayerLoggedOutEvent>()

        sut.onPlayerLoggedOut(event)

        verify { mockFeature.initTime() }
    }

    should("not call initTime on logout when playerCount > 1") {
        every { mockServer.playerCount } returns 2
        val event = mockk<PlayerEvent.PlayerLoggedOutEvent>()

        sut.onPlayerLoggedOut(event)

        verify(exactly = 0) { mockFeature.initTime() }
    }
})

