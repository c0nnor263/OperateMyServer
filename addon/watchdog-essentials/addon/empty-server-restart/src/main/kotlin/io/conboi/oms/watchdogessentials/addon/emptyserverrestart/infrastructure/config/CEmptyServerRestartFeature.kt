package io.conboi.oms.watchdogessentials.addon.emptyserverrestart.infrastructure.config

import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.config.FeatureConfigImpl
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.MAX_RETENTION_MINUTES
import io.conboi.oms.watchdogessentials.addon.emptyserverrestart.foundation.MIN_RETENTION_MINUTES

class CEmptyServerRestartFeature : FeatureConfigImpl() {
    companion object {
        const val NAME = "empty_server_restart"
    }

    override val name: String = NAME

    val countTime = s(
        "1h",
        "count_time",
        Comments.COUNT_TIME
    ) { value ->
        val countTime = value?.let {
            TimeFormatter.parseToDurationOrNull(it)
        }
        val minutes = countTime?.inWholeMinutes ?: return@s false
        minutes in MIN_RETENTION_MINUTES..MAX_RETENTION_MINUTES
    }

    object Comments {
        const val EMPTY_SERVER_RESTART =
            "Feature to automatically restart the server when it is empty for a certain period of time"
        const val COUNT_TIME =
            "The time over for restarting the server when it is empty. Default is 1h hour(\"1h\"). Minimum is 5 minutes(\"5m\")"
    }
}