package io.conboi.oms.common.foundation

data class CachedField<K, V>(
    val key: () -> K,
    val value: () -> V,
    val valueValidator: ((V) -> Boolean)? = null,
    val onUpdate: ((old: V, new: V) -> Unit)? = null,
    private var cachedKey: K = key.invoke(),
    private var cachedValue: V = value.invoke()
) {
    fun get(): V {
        val k = key.invoke()
        if (k != cachedKey) {
            cachedKey = k

            val newValue = value.invoke()
            if (valueValidator?.invoke(newValue) == false) {
                error("Value validation failed")
            }

            onUpdate?.invoke(cachedValue, newValue)
            cachedValue = newValue
        }
        return cachedValue
    }

    fun invalidate() {
        val newValue = value.invoke()
        onUpdate?.invoke(cachedValue, newValue)
        cachedKey = key.invoke()
        cachedValue = newValue
    }
}