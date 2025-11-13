package io.conboi.oms.watchdogessentials

import io.conboi.oms.api.OmsAddons
import io.conboi.oms.watchdogessentials.core.WatchDogEssentials
import io.conboi.oms.watchdogessentials.infrastructure.config.WEConfigs
import net.minecraftforge.fml.common.Mod

@Mod(WatchDogEssentials.MOD_ID)
object WatchDogEssentialsMod {
    init {
        WEConfigs.register()
        OmsAddons.register(WatchDogEssentialsAddon())
    }
}