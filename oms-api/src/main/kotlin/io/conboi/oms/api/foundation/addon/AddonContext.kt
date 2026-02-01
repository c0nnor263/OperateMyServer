package io.conboi.oms.api.foundation.addon

import io.conboi.oms.api.foundation.manager.FeatureManager
import io.conboi.oms.api.infrastructure.file.AddonPaths

/**
 * Runtime context of an OMS addon.
 *
 * This interface provides access to addon-specific infrastructure
 * and services supplied by OMS during initialization and runtime.
 *
 * The context is immutable from the addon perspective and serves as
 * the primary integration point between the addon and the OMS runtime.
 *
 * @property id the unique identifier of the addon
 * @property paths filesystem paths associated with the addon
 * @property featureManager feature manager used to register and control addon features
 */
interface AddonContext {
    val id: String
    val paths: AddonPaths
    val featureManager: FeatureManager
}
