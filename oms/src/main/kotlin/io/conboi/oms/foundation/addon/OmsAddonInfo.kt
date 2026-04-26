package io.conboi.oms.foundation.addon

import io.conboi.oms.api.foundation.info.OmsInfo
import io.conboi.oms.api.foundation.manager.FeatureManagerInfo

/**
 * Information snapshot describing an OMS addon.
 *
 * This data class represents immutable, structured information
 * about a registered addon and its associated infrastructure.
 *
 * It is intended for diagnostics, status reporting, and
 * external inspection
 *
 * @property data additional, addon-specific metadata
 * @property id the unique identifier of the addon
 * @property featureManagerInfo information about the addon's feature manager
 */
data class OmsAddonInfo(
    override val data: Map<String, Any> = emptyMap(),
    val id: String,
    val featureManagerInfo: FeatureManagerInfo
) : OmsInfo