package io.conboi.restartmyserver.infrastructure

import io.conboi.restartmyserver.infrastructure.config.ConfigBase
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus
import net.minecraftforge.fml.config.ModConfig
import thedarkcolour.kotlinforforge.forge.registerConfig
import java.util.function.Supplier

@EventBusSubscriber(bus = Bus.MOD)
object RMSConfigs {
    val CONFIGS: MutableMap<ModConfig.Type, ConfigBase> = hashMapOf()
    var server: CServer = registerNewConfig({ CServer() }, ModConfig.Type.SERVER)

    private fun <T : ConfigBase> registerNewConfig(factory: Supplier<T>, side: ModConfig.Type): T {
        val specPair = ForgeConfigSpec.Builder()
            .configure { builder ->
                factory.get().apply {
                    registerAll(builder)
                }
            }
        val config = specPair.getLeft()
        config.specification = specPair.getRight()
        CONFIGS[side] = config
        return config
    }

    fun register() {
        CONFIGS.forEach { (key, config) ->
            config.specification?.let { spec ->
                registerConfig(key, spec)
            }
        }
    }
}
