package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.CachedField

abstract class ConfigWatcher {
    private val configFields = mutableListOf<CachedField<*, *>>()
    var isConfigDirty = false
        protected set

    open fun flagConfigAsDirty() {
        isConfigDirty = true
    }

    open fun onConfigUpdated(event: OMSLifecycle.TickingEvent) {
        isConfigDirty = false
    }

    protected fun watchConfig() {
        configFields.forEach { it.watch() }
    }

    protected fun <K, V> configField(block: CachedField.Builder<K, V>.() -> Unit): CachedField<K, V> {
        val builder = CachedField.Builder<K, V>().apply(block)
        builder.observe = true
        val field = builder.build()
        configFields.add(field)
        return field
    }
}
