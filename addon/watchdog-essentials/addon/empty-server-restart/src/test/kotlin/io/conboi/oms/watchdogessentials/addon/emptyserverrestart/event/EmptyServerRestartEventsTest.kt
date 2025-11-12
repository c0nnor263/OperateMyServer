package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.ServerAccess
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.core.foundation.feature.WEFeatureManager
import io.conboi.oms.watchdogessentials.core.foundation.feature.we
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import net.minecraft.server.MinecraftServer
import net.minecraftforge.event.entity.player.PlayerEvent

class EmptyServerRestartEventsTest : FunSpec({

    val mockServer = mockk<MinecraftServer>(relaxed = true)
    val mockFeature = mockk<EmptyServerRestartFeature>(relaxed = true)
    val mockWeFeatureManager: WEFeatureManager = mockk<WEFeatureManager>()

    beforeEach {
        mockkObject(OMSFeatureManagers)

        every { OMSFeatureManagers.get<WEFeatureManager>(any()) } returns mockWeFeatureManager
        every {
            mockWeFeatureManager.getFeatureById<EmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME)
        } returns mockFeature

        mockkObject(ServerAccess)
        every { ServerAccess.getCurrentServer() } returns mockServer
    }

    afterEach {
        clearAllMocks()
    }

    test("should call clearTime on player login") {
        val event = mockk<PlayerEvent.PlayerLoggedInEvent>()
        EmptyServerRestartEvents.onPlayerLoggedIn(event)

        verify { mockFeature.clearTime() }
    }

    test("should call initTime on player logout if playerCount <= 1") {
        val event = mockk<PlayerEvent.PlayerLoggedOutEvent>()

        every { mockServer.playerCount } returns 1

        EmptyServerRestartEvents.onPlayerLoggedOut(event)

        verify { mockFeature.initTime() }
    }

    test("should not call initTime on player logout if playerCount > 1") {
        val event = mockk<PlayerEvent.PlayerLoggedOutEvent>()
        every { mockServer.playerCount } returns 3

        EmptyServerRestartEvents.onPlayerLoggedOut(event)

        verify(exactly = 0) { mockFeature.initTime() }
    }

    test("should not throw if feature is null on login/logout") {
        val loginEvent = mockk<PlayerEvent.PlayerLoggedInEvent>()
        val logoutEvent = mockk<PlayerEvent.PlayerLoggedOutEvent>()

        every { mockServer.playerCount } returns 1

        every {
            OMSFeatureManagers.we.getFeatureById<EmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME)
        } returns null

        // should not throw
        EmptyServerRestartEvents.onPlayerLoggedIn(loginEvent)
        EmptyServerRestartEvents.onPlayerLoggedOut(logoutEvent)
    }
})