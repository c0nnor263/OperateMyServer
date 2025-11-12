package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import net.minecraft.server.MinecraftServer

class ServerAccessTest : FunSpec({

    val server = mockk<MinecraftServer>()

    beforeTest {
        mockkObject(ServerAccess)
        every { ServerAccess.getCurrentServer() } returns server
    }

    afterTest {
        unmockkAll()
    }

    test("should return mocked server") {
        val result = ServerAccess.getCurrentServer()
        result shouldBe server
    }
})