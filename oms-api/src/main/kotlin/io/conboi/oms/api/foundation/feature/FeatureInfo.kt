package io.conboi.oms.api.foundation.feature

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
    // TODO: Maybe move implementation under OmsFeature
    interface Type {
        val id: String
        val localizedNameId: String
            get() = "oms.feature.$id"
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
