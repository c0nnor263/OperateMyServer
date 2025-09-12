package io.conboi.oms

import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.infrastructure.config.OMSConfigs
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(OperateMyServer.MOD_ID)
object OperateMyServerForge {

    init {
        OMSConfigs.register()
    }
}