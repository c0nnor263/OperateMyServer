package io.conboi.operatemyserver

import io.conboi.operatemyserver.infrastructure.config.OMSConfigs
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = "operatemyserver", bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod("operatemyserver")
object OperateMyServerForge {

    init {
        OMSConfigs.register()
        OMSFeatures.init()
    }
}