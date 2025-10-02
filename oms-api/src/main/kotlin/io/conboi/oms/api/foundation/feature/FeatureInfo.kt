package io.conboi.oms.api.foundation.feature

/**
 * Information about a feature, used for registration and prioritization.
 *
 * @property id The unique identifier of the feature
 * @property priority The priority of the feature
 */
data class FeatureInfo(
    val id: String,
    val priority: Priority = Priority.NONE,
) {
    /**
     * Priority levels for features. Higher priority features are processed first
     */
    enum class Priority {
        NONE,
        COMMON,
        CRITICAL
    }
}
