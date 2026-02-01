package io.conboi.oms.api.foundation.info

/**
 * Provides structured information snapshot for an OMS component.
 *
 * Implementations of this interface expose component-specific
 * information in the form of an [OmsInfo] instance.
 *
 * The returned information is expected to represent a current,
 * read-only snapshot of the component state and must not be used
 * to mutate internal logic.
 *
 * @param T the concrete type of information provided
 */
interface InfoProvider<out T : OmsInfo> {

    /**
     * Returns the current information snapshot of the component.
     *
     * Implementations should ensure that the returned object
     * reflects the latest available state at the time of invocation.
     *
     * @return a structured information object
     */
    fun info(): T
}
