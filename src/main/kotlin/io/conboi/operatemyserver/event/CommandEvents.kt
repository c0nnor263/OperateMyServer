package io.conboi.operatemyserver.event

import io.conboi.operatemyserver.elements.commands.OperateMyServerCommand
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = "operatemyserver")
object CommandEvents {
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        OperateMyServerCommand().register(event.dispatcher)
    }
}