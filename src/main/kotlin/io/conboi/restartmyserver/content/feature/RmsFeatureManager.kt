package io.conboi.restartmyserver.content.feature

import io.conboi.restartmyserver.foundation.feature.RmsFeature
import net.minecraft.server.MinecraftServer

object RmsFeatureManager {
    private val features = mutableListOf<RmsFeature<*>>()

    fun registerFeature(feature: RmsFeature<*>) {
        // TODO: Create verify feature fields
        features.add(feature)
    }

    fun getFeatures(): List<RmsFeature<*>> {
        return features
    }

    fun tickAll(server: MinecraftServer) {
        features.forEach { feature ->
            if (feature.isEnabled()) {
                feature.tick(server)
            }
        }
    }
}