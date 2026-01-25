package io.conboi.oms.event.listener

import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.elements.commands.OperateMyServerCommandBranch
import io.conboi.oms.elements.commands.vanilla.OverrideStopCommand
import io.conboi.oms.event.OMSInternal
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object CommandsListener {
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        FORGE_BUS.post(OMSInternal.Addon.PrepareEvent())
        OperateMyServerCommandBranch().register(event.dispatcher)
        OverrideStopCommand().register(event.dispatcher)
    }
}