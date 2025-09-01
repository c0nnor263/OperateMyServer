package io.conboi.operatemyserver

import io.conboi.operatemyserver.common.OperateMyServer
import io.conboi.operatemyserver.infrastructure.config.OMSConfigs
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(OperateMyServer.MOD_ID)
object OperateMyServerForge {

    init {
        OMSConfigs.register()
        OMSFeatures.init()
    }
}