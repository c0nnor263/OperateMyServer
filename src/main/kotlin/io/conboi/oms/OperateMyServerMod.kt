package io.conboi.oms

import io.conboi.oms.api.OperateMyServer
import io.conboi.oms.common.infrastructure.config.OMSConfigs
import io.conboi.oms.infrastructure.config.OMSFeatureConfigs
import net.minecraftforge.fml.common.Mod

@Mod(OperateMyServer.MOD_ID)
object OperateMyServerMod {
    init {
        OMSFeatureConfigs.register()
        OMSConfigs.register()
    }
}