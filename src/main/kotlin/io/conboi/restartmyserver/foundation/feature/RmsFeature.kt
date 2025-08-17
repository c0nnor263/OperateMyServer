package io.conboi.restartmyserver.foundation.feature

import io.conboi.restartmyserver.infrastructure.CServer
import io.conboi.restartmyserver.infrastructure.config.FeatureConfigBase
import net.minecraft.server.MinecraftServer

// TODO: Add Feature Priority for execution order in RmsFeatureManager.tickAll
// TODO: Create verify feature fields
abstract class RmsFeature<out T : FeatureConfigBase>(private val serverConfig: CServer, private val type: FeatureType) {
    val featureConfig: T get() = serverConfig.getConfigByFeatureType(type)

    fun isEnabled(): Boolean = featureConfig.enabled.get()

    abstract fun tick(server: MinecraftServer)
}