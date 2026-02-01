package io.conboi.oms.api.foundation.addon

import io.conboi.oms.api.foundation.manager.FeatureManager
import io.conboi.oms.api.infrastructure.file.AddonPaths

/**
 * Specification for constructing an [AddonContext].
 *
 * This specification allows addons to customize how their runtime context
 * is created by overriding selected factory functions.
 *
 * The specification is typically configured inside
 * [OmsAddon.configureContext].
 *
 * Any factory not explicitly provided will fall back to
 * the default OMS implementation.
 *
 * @property id the unique identifier of the addon
 * @property pathsFactory factory for creating [AddonPaths]
 * @property featureManagerFactory factory for creating a [FeatureManager]
 */
data class AddonContextSpec(
    val id: String,
    val pathsFactory: (() -> AddonPaths)? = null,
    val featureManagerFactory: (() -> FeatureManager)? = null,
)
