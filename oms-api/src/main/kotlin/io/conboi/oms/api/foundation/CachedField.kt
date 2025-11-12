package io.conboi.oms.api.foundation

class CachedField<K, V> internal constructor(
    val key: () -> K,
    val value: () -> V,
    val validator: ((V) -> Boolean)? = null,
    val onUpdate: ((old: V?, new: V) -> Unit)? = null,
    val observe: Boolean = false,
    private var cachedKey: K? = null,
    private var cachedValue: V? = null
) {
    companion object {
        fun <K, V> builder() = Builder<K, V>()
    }

    class Builder<K, V> {
        lateinit var key: () -> K
        lateinit var value: () -> V
        var validator: ((V) -> Boolean)? = null
        var onUpdate: ((old: V?, new: V) -> Unit)? = null
        var observe: Boolean = false

        fun build(): CachedField<K, V> {
            return CachedField(key, value, validator, onUpdate, observe)
        }
    }

    fun get(): V {
        val k = key.invoke()
        if (k != cachedKey) {
            cachedKey = k

            val newValue = value.invoke()
            if (validator?.invoke(newValue) == false) {
                throw IllegalArgumentException("CachedField validation failed: value = $newValue")
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

    fun watch() {
        if (observe) get()
    }
}


inline fun <K, V> cachedField(block: CachedField.Builder<K, V>.() -> Unit): CachedField<K, V> {
    return CachedField.Builder<K, V>().apply(block).build()
}