package io.conboi.oms.api.foundation

/**
 * A cached, key-dependent value holder.
 *
 * A [CachedField] computes its value lazily using the provided [value] function
 * and caches the result as long as the computed [key] remains unchanged.
 *
 * When the key changes, the cached value is invalidated and recomputed.
 * Optional hooks allow validation, update observation, and controlled recomputation.
 *
 * This utility is intended for lightweight, deterministic caching of derived values
 * within OMS components.
 *
 * @param K the type of the cache key
 * @param V the type of the cached value
 *
 * @property key supplies the current cache key
 * @property value computes the cached value
 * @property validator optional validator invoked after recomputation
 * @property onUpdate optional callback invoked when the cached value changes
 * @property observe whether the field should eagerly recompute on [watch]
 */
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

        /**
         * Creates a builder for constructing a [CachedField].
         */
        fun <K, V> builder() = Builder<K, V>()
    }

    /**
     * Builder for configuring and creating a [CachedField].
     */
    class Builder<K, V> {
        lateinit var key: () -> K
        lateinit var value: () -> V
        var validator: ((V) -> Boolean)? = null
        var onUpdate: ((old: V?, new: V) -> Unit)? = null
        var observe: Boolean = false

        /**
         * Builds the [CachedField] instance.
         */
        fun build(): CachedField<K, V> {
            return CachedField(key, value, validator, onUpdate, observe)
        }
    }

    /**
     * Returns the cached value.
     *
     * If the cache key has changed since the last access,
     * the value is recomputed and the cache is updated.
     *
     * @return the cached value
     *
     * @throws IllegalStateException if the value cannot be initialized
     * @throws IllegalArgumentException if validation fails
     */
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

        return cachedValue
            ?: throw IllegalStateException("Cached value is not initialized")
    }

    /**
     * Returns a snapshot of the cached value without throwing exceptions.
     *
     * This method never updates the cache and is safe to use
     * in diagnostic or best-effort contexts.
     *
     * @return the cached value, or `null` if computation fails
     */
    fun getSnapshotSafely(): V? {
        return try {
            cachedValue ?: value.invoke()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Forces cache invalidation and recomputation.
     *
     * The cache key is refreshed and the value is recomputed
     * regardless of whether the key has changed.
     */
    fun invalidate() {
        val newValue = invalidateValue()
        onUpdate?.invoke(cachedValue, newValue)
        cachedKey = key.invoke()
        cachedValue = newValue
    }

    /**
     * Recomputes the cached value and applies validation.
     */
    private fun invalidateValue(): V {
        val computed = value.invoke()

        if (validator?.invoke(computed) == false) {
            throw IllegalArgumentException(
                "CachedField validation failed: value = $computed"
            )
        }

        return computed
    }

    /**
     * Triggers observation of this field.
     *
     * If [observe] is enabled, this method causes the value
     * to be computed and cached.
     */
    fun watch() {
        if (observe) get()
    }
}

/**
 * Creates a [CachedField] using a declarative builder block.
 *
 * @param block configuration block for the [CachedField.Builder]
 * @return a configured [CachedField] instance
 */
inline fun <K, V> cachedField(
    block: CachedField.Builder<K, V>.() -> Unit
): CachedField<K, V> {
    return CachedField.Builder<K, V>()
        .apply(block)
        .build()
}
