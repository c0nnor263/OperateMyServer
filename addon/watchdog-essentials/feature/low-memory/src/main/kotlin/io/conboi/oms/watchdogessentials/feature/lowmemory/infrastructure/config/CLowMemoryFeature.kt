package io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.config

import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.config.FeatureConfigImpl
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.CriticalAction
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.MemorySnapshotHistory
import io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.config.cooldown.CCooldowns

class CLowMemoryFeature : FeatureConfigImpl() {
    companion object {
        const val NAME = "low_memory"
    }

    override val name: String = NAME

    val cooldowns =
        nested(
            0,
            { CCooldowns() },
            CCooldowns.Comments.COOLDOWNS
        )

    val createHeapDumpOnAction = b(
        false,
        "create_heap_dump_on_action",
        Comments.CREATE_HEAP_DUMP_ON_ACTION
    )

    val startupCheck = b(
        true,
        "startup_check",
        Comments.STARTUP_CHECK
    )

    val averagingWindow = s(
        "5m",
        "averaging_window",
        Comments.AVERAGING_WINDOW,
    ) { value ->
        val averagingWindow = value?.let {
            TimeFormatter.parseToDurationOrNull(it)
        } ?: return@s false
        averagingWindow in MemorySnapshotHistory.MIN_RETENTION_MINUTES..MemorySnapshotHistory.MAX_RETENTION_MINUTES
    }

    val availableThresholdPercent = f(
        10.0F,
        min = 1.0F,
        max = 50.0F,
        "available_threshold_percent",
        Comments.AVAILABLE_THRESHOLD_PERCENT
    )

    val criticalAction = s(
        CriticalAction.RESTART.name,
        "critical_action",
        Comments.CRITICAL_ACTION
    ) { value ->
        runCatching {
            CriticalAction.valueOf(value ?: "")
        }.isSuccess
    }

    object Comments {
        const val LOW_MEMORY =
        "This feature monitors the server's available memory and takes action if it falls below a certain threshold. It is designed to help prevent crashes due to low-memory conditions."
        const val CREATE_HEAP_DUMP_ON_ACTION =
            "If true, the server will create a heap dump when it is under low memory conditions. This can be useful for debugging memory issues. Default is false."
        const val STARTUP_CHECK =
        "If true, the server will check its available memory at startup and warn if it is below the recommended threshold. Default is true."
        const val AVERAGING_WINDOW =
        "The time over which the server's available memory is averaged. Default is 5 minutes. Minimum is 1 minute, maximum is 10 minutes."
        const val AVAILABLE_THRESHOLD_PERCENT =
            "The percentage of available memory below which the server is considered to be under low memory conditions. Default is 10%. Minimum is 1%, maximum is 50%."
        val CRITICAL_ACTION =
            "The action to take when the server is under low memory conditions. Options are: ${
                CriticalAction.entries.joinToString(
                    ", "
                ) { it.name }
            }. Default is ${CriticalAction.RESTART}."
    }
}