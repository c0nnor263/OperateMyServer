package io.conboi.oms.api.foundation.feature

interface FeatureRegistry {
    fun register(feature: OmsFeature<*>)
}