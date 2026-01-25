package io.conboi.oms.api.foundation.feature

/**
 * Priority levels for OMS features.
 *
 * Feature priority determines the order in which features are
 * processed and receive lifecycle callbacks.
 *
 * Higher priority values are processed before lower priority ones.
 */
enum class Priority {

    /**
     * No specific priority.
     *
     * Features with this priority are processed after
     * higher-priority features.
     */
    NONE,

    /**
     * Default priority for standard features.
     */
    COMMON,

    /**
     * High priority reserved for critical features.
     *
     * Features with this priority are processed first and
     * should be used sparingly.
     */
    CRITICAL
}
