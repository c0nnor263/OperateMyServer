package io.conboi.oms.api.permission

import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer

/**
 * Represents the vanilla Minecraft command permission levels.
 */
enum class PermissionLevel(val value: Int) {
    ALL(0),
    MODERATOR(1),
    GAMEMASTER(2),
    ADMIN(3),
    OWNER(4)
}

fun CommandSourceStack.hasPermission(level: PermissionLevel): Boolean {
    return hasPermission(level.value)
}

fun ServerPlayer.hasPermission(level: PermissionLevel): Boolean {
    return hasPermissions(level.value)
}