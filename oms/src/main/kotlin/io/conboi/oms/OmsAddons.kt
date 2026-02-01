package io.conboi.oms

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContextSpec
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.api.foundation.info.InfoProvider
import io.conboi.oms.api.foundation.manager.FeatureManager
import io.conboi.oms.content.StopManager
import io.conboi.oms.foundation.addon.AddonInstance
import io.conboi.oms.foundation.addon.DefaultAddonContextFactory
import io.conboi.oms.infrastructure.file.OMSRootPath
import java.nio.file.Path
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

internal object OmsAddons : InfoProvider<OmsAddonsInfo> {
    private val registry = mutableMapOf<String, AddonInstance>()

    override fun info(): OmsAddonsInfo {
        return OmsAddonsInfo(
            addonsInfo = registry.values.map { it.info() }
        )
    }

    private fun validateAddonId(id: String) {
        require(!registry.containsKey(id)) {
            "Addon '$id' already registered"
        }
        require(id.matches(Regex("[a-z0-9_\\-]+"))) {
            "Invalid addon id: $id. It must only contain lowercase letters, numbers, underscores, and hyphens."
        }
    }

    fun onPrepare(registryAddons: List<OmsAddon>) {
        registryAddons.forEach { addon ->
            val id = addon.id
            validateAddonId(id)

            val baseSpec = AddonContextSpec(id = id)
            val finalSpec = addon.configureContext(baseSpec)
            require(finalSpec.id == id) {
                "AddonContextSpec.id cannot be overridden"
            }

            val context = DefaultAddonContextFactory.create(finalSpec)
            val featureManager = context.featureManager
            addon.onRegisterFeatures(context)
            featureManager.freeze()
            registerForgeListeners(featureManager)

            registry[id] = AddonInstance(
                addon = addon,
                context = context,
            )
        }
    }

    fun onServerReady(server: MinecraftServer) {
        StopManager.installHook()
        OMSRootPath.init(server)
        onInitializeOmsRoot(OMSRootPath.root)
        onRegisterConfigs()
    }

    fun get(id: String): AddonInstance? = registry[id]

    fun onInitializeOmsRoot(omsRootPath: Path) {
        forEachInstance { instance ->
            instance.context.paths.onInitializeOmsRoot(omsRootPath)
        }
    }

    fun onRegisterConfigs() {
        forEachInstance { instance ->
            instance.context.featureManager.onRegisterConfig()
        }
    }

    fun onOmsStarted(event: OMSLifecycle.StartingEvent) {
        forEachInstance { instance ->
            instance.context.featureManager.onStartingEvent(event, instance.context)
        }
    }

    fun onOmsTick(event: OMSLifecycle.TickingEvent) {
        forEachInstance { instance ->
            instance.context.featureManager.onTickingEvent(event, instance.context)
        }
    }

    fun onOmsStopping(event: OMSLifecycle.StoppingEvent) {
        forEachInstance { instance ->
            instance.context.featureManager.onStoppingEvent(event, instance.context)
        }
    }

    fun forEachInstance(action: (AddonInstance) -> Unit) {
        registry.values.forEach(action)
    }

    private fun registerForgeListeners(fm: FeatureManager) {
        fm.features().forEach { feature ->
            feature.createEventListeners().forEach { listener ->
                FORGE_BUS.register(listener)
            }
        }
    }
}