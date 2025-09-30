package io.conboi.oms

import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.common.infrastructure.config.OMSConfigs
import io.conboi.oms.infrastructure.config.OMSFeatureConfigs
import net.minecraftforge.fml.common.Mod

@Mod(OperateMyServer.MOD_ID)
object OperateMyServerForge {
    init {
        OMSFeatureConfigs.register()
        OMSConfigs.register()
    }
}