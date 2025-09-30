package io.conboi.oms.event

import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.common.event.OMSLifecycleInternal
import io.conboi.oms.elements.commands.OperateMyServerCommandBranch
import io.conboi.oms.elements.commands.vanilla.OverrideStopCommand
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
object CommandEvents {
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        FORGE_BUS.post(OMSLifecycleInternal.RegisterFeaturesEvent())
        OperateMyServerCommandBranch().register(event.dispatcher)
        OverrideStopCommand().register(event.dispatcher)
    }
}