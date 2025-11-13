package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.event

import io.conboi.oms.api.OmsAddons
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.EmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.ServerAccess
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config.CEmptyServerRestartFeature
import io.conboi.oms.watchdogessentials.core.WatchDogEssentials
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
    val mockAddon = mockk<OmsAddon>()
    val mockFeature = mockk<EmptyServerRestartFeature>(relaxed = true)

    beforeSpec {
        mockkObject(OmsAddons)
        mockkObject(ServerAccess)
    }

    beforeEach {
        every { ServerAccess.getCurrentServer() } returns mockServer
        every { OmsAddons.get(WatchDogEssentials.MOD_ID) } returns mockAddon
        every { mockAddon.getFeatureById<EmptyServerRestartFeature>(CEmptyServerRestartFeature.NAME) } returns mockFeature
    }

    afterEach {
        clearAllMocks()
    }

    should("call clearTime on player login when feature exists") {
        val event = mockk<PlayerEvent.PlayerLoggedInEvent>()

        EmptyServerRestartEvents.onPlayerLoggedIn(event)

        verify { mockFeature.clearTime() }
    }

    should("not throw when addon is null on login") {
        every { OmsAddons.get(WatchDogEssentials.MOD_ID) } returns null

        val event = mockk<PlayerEvent.PlayerLoggedInEvent>()

        EmptyServerRestartEvents.onPlayerLoggedIn(event)

        verify(exactly = 0) { mockFeature.clearTime() }
    }

    should("not throw when feature is null on login") {
        every { mockAddon.getFeatureById<EmptyServerRestartFeature>(any()) } returns null

        val event = mockk<PlayerEvent.PlayerLoggedInEvent>()

        EmptyServerRestartEvents.onPlayerLoggedIn(event)

        verify(exactly = 0) { mockFeature.clearTime() }
    }

    should("call initTime on logout when playerCount <= 1") {
        every { mockServer.playerCount } returns 1

        val event = mockk<PlayerEvent.PlayerLoggedOutEvent>()

        EmptyServerRestartEvents.onPlayerLoggedOut(event)

        verify { mockFeature.initTime() }
    }

    should("not call initTime on logout when playerCount > 1") {
        every { mockServer.playerCount } returns 2

        val event = mockk<PlayerEvent.PlayerLoggedOutEvent>()

        EmptyServerRestartEvents.onPlayerLoggedOut(event)

        verify(exactly = 0) { mockFeature.initTime() }
    }

    should("not throw when addon is null on logout") {
        every { OmsAddons.get(WatchDogEssentials.MOD_ID) } returns null
        every { mockServer.playerCount } returns 1

        val event = mockk<PlayerEvent.PlayerLoggedOutEvent>()

        EmptyServerRestartEvents.onPlayerLoggedOut(event)

        verify(exactly = 0) { mockFeature.initTime() }
    }

    should("not throw when feature is null on logout") {
        every { mockServer.playerCount } returns 1
        every { mockAddon.getFeatureById<EmptyServerRestartFeature>(any()) } returns null

        val event = mockk<PlayerEvent.PlayerLoggedOutEvent>()

        EmptyServerRestartEvents.onPlayerLoggedOut(event)

        verify(exactly = 0) { mockFeature.initTime() }
    }
})
