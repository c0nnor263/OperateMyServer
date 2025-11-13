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

            val newValue = invalidateValue()

            if (cachedValue != null) {
                onUpdate?.invoke(cachedValue, newValue)
            }
            cachedValue = newValue
        }
        return cachedValue ?: throw IllegalStateException("Cached value is not initialized")
    }

    fun getSnapshotSafely(): V? {
        return try {
            if (cachedValue == null) {
                value.invoke()
            } else cachedValue
        } catch (e: Exception) {
            null
        }
    }

    fun invalidate() {
        val newValue = invalidateValue()
        onUpdate?.invoke(cachedValue, newValue)
        cachedKey = key.invoke()
        cachedValue = newValue
    }

    private fun invalidateValue(): V {
        val value = value.invoke()
        if (validator?.invoke(value) == false) {
            throw IllegalArgumentException("CachedField validation failed: value = $value")
        }
        return value
    }

    fun watch() {
        if (observe) get()
    }
}


inline fun <K, V> cachedField(block: CachedField.Builder<K, V>.() -> Unit): CachedField<K, V> {
    return CachedField.Builder<K, V>().apply(block).build()
}