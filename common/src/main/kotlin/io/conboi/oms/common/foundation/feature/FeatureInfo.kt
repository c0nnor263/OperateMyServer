package io.conboi.oms.common.foundation.feature

/**
 * Information about a feature, used for registration and prioritization.
 *
 * @property type The type of the feature.
 * @property priority The priority of the feature
 */
data class FeatureInfo(
    val type: Type,
    val priority: Priority = Priority.NONE,
) {
    // TODO: Maybe change to interface if we need more flexibility
    enum class Type {
        AUTO_RESTART,
        LOW_TPS
    }

    /**
     * Priority levels for features. Higher priority features are processed first
     */
    enum class Priority {
        NONE,
        COMMON,
        CRITICAL
    }
}
