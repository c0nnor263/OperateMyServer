package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import net.minecraft.server.MinecraftServer

class ServerAccessTest : ShouldSpec({

    val mockServer = mockk<MinecraftServer>()

    beforeTest {
        mockkObject(ServerAccess)
        every { ServerAccess.getCurrentServer() } returns mockServer
    }

    afterTest {
        clearAllMocks()
    }

    should("return mocked server") {
        val result = ServerAccess.getCurrentServer()
        result shouldBe mockServer
    }
})