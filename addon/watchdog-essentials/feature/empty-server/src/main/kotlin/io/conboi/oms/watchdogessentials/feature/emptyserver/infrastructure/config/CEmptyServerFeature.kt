package io.conboi.oms.watchdogessentials.feature.emptyserver.infrastructure.config

import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.config.FeatureConfigImpl
import io.conboi.oms.watchdogessentials.feature.emptyserver.foundation.MAX_RETENTION_DURATION
import io.conboi.oms.watchdogessentials.feature.emptyserver.foundation.MIN_RETENTION_DURATION

class CEmptyServerFeature : FeatureConfigImpl() {
    companion object {
        const val NAME = "empty_server"
    }

    override val name: String = NAME

    val countTime = s(
        "1h",
        "count_time",
        Comments.COUNT_TIME
    ) { value ->
        val countTime = value?.let {
            TimeFormatter.parseToDurationOrNull(it)
        } ?: return@s false
        countTime in MIN_RETENTION_DURATION..MAX_RETENTION_DURATION
    }

    val shouldRestart = b(
        true,
        "should_restart",
        Comments.SHOULD_RESTART
    )

    object Comments {
        const val EMPTY_SERVER =
            "Feature to automatically shutdown the server when it is empty for a certain period of time"
        const val COUNT_TIME =
            "How long the server must remain empty before it stops. Default is 1h. Minimum is 5m."
        const val SHOULD_RESTART =
            "Whether the server should be restarted after shutdown. Default is true"
    }
}