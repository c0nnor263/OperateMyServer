package io.conboi.oms.core.event

import io.conboi.oms.api.OperateMyServer
import io.conboi.oms.core.elements.commands.OperateMyServerCommandBranch
import io.conboi.oms.core.elements.commands.vanilla.OverrideStopCommand
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object CommandEvents {
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        FORGE_BUS.post(OMSLifecycleInternal.Feature.RegisterEvent())
        OperateMyServerCommandBranch().register(event.dispatcher)
        OverrideStopCommand().register(event.dispatcher)
    }
}