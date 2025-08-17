package io.conboi.restartmyserver.event

import io.conboi.restartmyserver.RestartMyServer
import io.conboi.restartmyserver.content.feature.AutoRestartFeature
import io.conboi.restartmyserver.content.feature.RmsFeatureManager
import io.conboi.restartmyserver.infrastructure.RMSConfigs
import io.conboi.restartmyserver.util.TickTimer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.server.ServerLifecycleHooks

@Mod.EventBusSubscriber(modid = RestartMyServer.MOD_ID)
object RestartMyServerEventHandler {
    private val tickTimer = TickTimer()

    @SubscribeEvent
    fun onServerStarted(e: ServerStartedEvent) {
        RmsFeatureManager.registerFeature(AutoRestartFeature(RMSConfigs.server))
    }

    @SubscribeEvent
    fun onServerTick(e: TickEvent.ServerTickEvent) {
        if (e.phase != TickEvent.Phase.END) return
        val server = ServerLifecycleHooks.getCurrentServer() ?: run {
            RestartMyServer.LOGGER.info("RestartMyServerEventHandler: Server is null, skipping tick")
            return
        }

        if (!tickTimer.shouldFire(server.tickCount)) return
        RmsFeatureManager.tickAll(server)
    }
}