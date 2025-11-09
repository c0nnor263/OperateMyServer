package io.conboi.oms.api

import io.conboi.oms.api.foundation.feature.FeatureManager

object OMSFeatureManagers {
    private var registry = mutableMapOf<String, FeatureManager>()

    val oms: FeatureManager = object : FeatureManager() {
        override val modId: String = OperateMyServer.MOD_ID

        init {
            this@OMSFeatureManagers.registry[getFullId()] = this
        }
    }

    fun register(manager: FeatureManager) {
        validateId(manager)
        registry[manager.getFullId()] = manager
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : FeatureManager> get(id: String): T? = registry[id] as? T?

    fun runForEach(action: FeatureManager.() -> Unit) {
        registry.values.forEach(action)
    }

    private fun validateId(manager: FeatureManager) {
        val id = manager.getFullId()
        require(id.isNotBlank()) { "FeatureManager id cannot be blank" }
        require(id.all { it.isLowerCase() || it.isDigit() || it == '_' || it == '-' || it == ':' }) {
            "FeatureManager id can only contain lowercase letters, digits, underscores, hyphens and colons"
        }

        val modId = manager.modId
        val name = manager.name
        require(modId.isNotBlank() && name.isNotBlank()) { "FeatureManager id must be in the format 'modid:name'" }
        require(modId != OperateMyServer.MOD_ID) {
            "FeatureManager with id '${OperateMyServer.MOD_ID}' cannot be registered, reserved for OMS"
        }

        require(!registry.containsKey(id)) {
            "FeatureManager with id '${id}' is already registered"
        }
    }

}