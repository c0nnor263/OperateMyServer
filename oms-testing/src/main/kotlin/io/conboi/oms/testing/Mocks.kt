package io.conboi.oms.testing

import io.mockk.every
import io.mockk.mockk
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.PlayerList

fun createMockPlayer(permissionLevel: Int = 4, playerList: PlayerList? = null): ServerPlayer {
    return mockk(relaxed = true) {
        every { name.string } returns "TestPlayer"
        every { hasPermissions(any()) } answers {
            firstArg<Int>() <= permissionLevel
        }
        if (playerList != null) {
            val existingMockPlayers = playerList.players
            every { playerList.players } returns existingMockPlayers + listOf(this)
        }
    }
}