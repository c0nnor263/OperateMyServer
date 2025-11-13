package io.conboi.oms.api

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.api.foundation.addon.OmsAddonsInfo
import io.conboi.oms.api.foundation.info.InfoProvider
import java.nio.file.Path

object OmsAddons : InfoProvider<OmsAddonsInfo> {
    private val registry = mutableMapOf<String, OmsAddon>()

    // TODO: Maybe keep one frozen state per addon?
    private var frozen = false

    override fun info(): OmsAddonsInfo {
        return OmsAddonsInfo(
            addonsInfo = registry.values.map { it.info() }
        )
    }

    /**
     * Register an [OmsAddon]. Must be called during @Mod initialization.
     */
    fun register(addon: OmsAddon) {
        val id = addon.id
        check(!frozen) {
            "OmsAddons.register(${id}) called after freeze. " +
                    "Register addons only in @Mod init."
        }
        require(!registry.containsKey(id)) {
            "Addon '$id' already registered"
        }
        registry[id] = addon
    }

    fun get(id: String): OmsAddon? = registry[id]

    fun onInitializeOmsRoot(omsRootPath: Path) {
        forEachAddon { addon -> addon.onInitializeOmsRoot(omsRootPath) }
    }

    // TODO: Think about freeze state here
    fun onRegisterConfigs() {
        forEachAddon { addon -> addon.onRegisterConfigs() }
    }

    fun onRegisterFeatures() {
        if (frozen) return
        forEachAddon { addon -> addon.onRegisterFeatures() }
    }

    fun onOmsStarted(event: OMSLifecycle.StartingEvent) = forEachAddon { addon -> addon.onOmsStarted(event) }
    fun onOmsTick(event: OMSLifecycle.TickingEvent) = forEachAddon { addon -> addon.onOmsTick(event) }
    fun onOmsStopping(event: OMSLifecycle.StoppingEvent) = forEachAddon { addon -> addon.onOmsStopping(event) }

    fun freeze() {
        if (frozen) return
        frozen = true
//        LOG.info("OmsAddons frozen with ${addons.size} addons: ${addons.keys}")
        registry.values.forEach { it.onFreeze() }
    }

    fun forEachAddon(action: (OmsAddon) -> Unit) {
        registry.values.forEach(action)
    }
}
