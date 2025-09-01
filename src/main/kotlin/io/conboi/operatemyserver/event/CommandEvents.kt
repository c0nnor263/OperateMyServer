package io.conboi.operatemyserver.event

import io.conboi.operatemyserver.common.OperateMyServer
import io.conboi.operatemyserver.elements.commands.OperateMyServerCommand
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
object CommandEvents {
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        OperateMyServerCommand().register(event.dispatcher)
    }
}