package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation

import net.minecraft.server.MinecraftServer
import net.minecraftforge.server.ServerLifecycleHooks

object ServerAccess {
    fun getCurrentServer(): MinecraftServer = ServerLifecycleHooks.getCurrentServer()
}