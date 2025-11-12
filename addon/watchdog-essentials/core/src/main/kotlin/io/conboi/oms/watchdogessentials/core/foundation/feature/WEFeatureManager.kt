package io.conboi.oms.watchdogessentials.core.foundation.feature

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.foundation.feature.FeatureManager
import io.conboi.oms.watchdogessentials.core.WatchDogEssentials

object WEFeatureManager : FeatureManager() {
    override val modId: String = WatchDogEssentials.MOD_ID

    init {
        OMSFeatureManagers.register(this)
    }
}

val OMSFeatureManagers.we: WEFeatureManager
    get() = get(WEFeatureManager.getFullId()) ?: WEFeatureManager