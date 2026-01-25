package io.conboi.oms

import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.foundation.addon.AddonInstance
import io.conboi.oms.infrastructure.config.OMSConfigs
import net.minecraftforge.fml.common.Mod

@Mod(OperateMyServer.MOD_ID)
object OperateMyServerMod {
    init {
        OMSConfigs.register()
    }
}

internal val OmsAddons.oms: AddonInstance
    get() = get(OperateMyServer.MOD_ID) ?: error("Operate My Server addon instance not found")