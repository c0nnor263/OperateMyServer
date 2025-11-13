package io.conboi.oms

import io.conboi.oms.api.OmsAddons
import io.conboi.oms.core.OperateMyServer
import io.conboi.oms.infrastructure.config.OMSConfigs
import net.minecraftforge.fml.common.Mod

@Mod(OperateMyServer.MOD_ID)
object OperateMyServerMod {
    init {
        OMSConfigs.register()
        OmsAddons.register(OperateMyServerAddon())
    }
}