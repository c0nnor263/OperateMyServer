package io.conboi.restartmyserver.elements.commands

import io.conboi.restartmyserver.RestartMyServer
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = RestartMyServer.MOD_ID)
object CommandBusHook {
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        RestartCommand.register(event.dispatcher)
    }
}