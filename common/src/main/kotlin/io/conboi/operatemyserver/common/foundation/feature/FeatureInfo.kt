package io.conboi.operatemyserver.common.foundation.feature

/**
 * Information about a feature, used for registration and prioritization.
 *
 * @property type The type of the feature.
 * @property priority The priority of the feature; lower values indicate higher priority. Default is 0.
 */
data class FeatureInfo(
    val type: Type,
    val priority: Int = 0
) {
    enum class Type {
        AUTO_RESTART,
        LOW_TPS
    }
}
