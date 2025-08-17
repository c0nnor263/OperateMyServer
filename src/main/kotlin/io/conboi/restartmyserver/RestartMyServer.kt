package io.conboi.restartmyserver

import io.conboi.restartmyserver.RestartMyServer.MOD_ID
import io.conboi.restartmyserver.infrastructure.RMSConfigs
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(MOD_ID)
object RestartMyServer {
    const val MOD_ID: String = "restartmyserver"

    val LOGGER: Logger = LogManager.getLogger(MOD_ID)

    init {
        LOGGER.log(Level.INFO, "Hello world!")
        RMSConfigs.register()
    }
}