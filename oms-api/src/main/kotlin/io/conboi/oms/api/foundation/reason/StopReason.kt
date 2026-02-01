package io.conboi.oms.api.foundation.reason

/**
 * Represents a logical reason for stopping or restarting an OMS-controlled process.
 *
 * Stop reasons are used to describe *why* a stop-related action was requested
 * (for example, scheduled restart, low TPS, manual command, or crash recovery).
 *
 * They are intended to be stable identifiers suitable for logging,
 * diagnostics, and user-facing messages.
 *
 * @property addonId the identifier of the addon defining this stop reason
 * @property name the logical name of the stop reason
 * @property messageId localization key derived from [addonId] and [name]
 */
interface StopReason {

    /**
     * Identifier of the addon that owns this stop reason.
     */
    val addonId: String

    /**
     * Logical name of the stop reason.
     *
     * This value should be stable and suitable for use in identifiers.
     */
    val name: String

    /**
     * Localization key associated with this stop reason.
     *
     * The default format is:
     * ```
     * <addonId>.stop_reason.<name>
     * ```
     */
    val messageId: String
        get() = "$addonId.stop_reason.$name"
}
