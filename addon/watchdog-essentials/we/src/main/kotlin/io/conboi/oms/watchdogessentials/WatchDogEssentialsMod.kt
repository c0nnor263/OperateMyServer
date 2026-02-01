package io.conboi.oms.watchdogessentials

import io.conboi.oms.watchdogessentials.common.WatchDogEssentials
import io.conboi.oms.watchdogessentials.infrastructure.config.WEConfigs
import net.minecraftforge.fml.common.Mod

@Mod(WatchDogEssentials.MOD_ID)
object WatchDogEssentialsMod {
    init {
        WEConfigs.register()
    }
}