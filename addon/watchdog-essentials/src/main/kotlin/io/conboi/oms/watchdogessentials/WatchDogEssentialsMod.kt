package io.conboi.oms.watchdogessentials

import io.conboi.oms.watchdogessentials.core.WatchDogEssentials
import io.conboi.oms.watchdogessentials.infrastructure.config.WEConfigs
import io.conboi.oms.watchdogessentials.infrastructure.config.WEFeatureConfigs
import net.minecraftforge.fml.common.Mod

@Mod(WatchDogEssentials.MOD_ID)
object WatchDogEssentialsMod {

    init {
        WEFeatureConfigs.register()
        WEConfigs.register()
    }
}