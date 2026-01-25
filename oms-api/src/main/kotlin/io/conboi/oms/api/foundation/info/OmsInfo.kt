package io.conboi.oms.api.foundation.info

/**
 * Represents structured information snapshot of an OMS component.
 *
 * This interface is intended to expose arbitrary, read-only metadata
 * about a component in a generic key-value form.
 *
 * Typical use cases include diagnostics, status reporting,
 * logging, or external integrations.
 *
 * @property data a map containing component-specific information entries
 */
interface OmsInfo {

    /**
     * Key-value data representing the current state or metadata
     * of the component.
     *
     * Values may be `null` if the information is unavailable or
     * not applicable.
     */
    val data: Map<String, Any?>
}
