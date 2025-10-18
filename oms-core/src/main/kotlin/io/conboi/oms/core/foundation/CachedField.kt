package io.conboi.oms.core.foundation

data class CachedField<K, V>(
    val key: () -> K,
    val value: () -> V,
    val valueValidator: ((V) -> Boolean)? = null,
    val onUpdate: ((old: V?, new: V) -> Unit)? = null,
    private var cachedKey: K? = null,
    private var cachedValue: V? = null
) {
    fun get(): V {
        val k = key.invoke()
        if (k != cachedKey) {
            cachedKey = k

            val newValue = value.invoke()
            if (valueValidator?.invoke(newValue) == false) {
                error("Value validation failed")
            }

            if (cachedValue != null) {
                onUpdate?.invoke(cachedValue, newValue)
            }
            cachedValue = newValue
        }
        return cachedValue ?: throw IllegalStateException("Cached value is not initialized")
    }

    fun invalidate() {
        val newValue = value.invoke()
        onUpdate?.invoke(cachedValue, newValue)
        cachedKey = key.invoke()
        cachedValue = newValue
    }
}