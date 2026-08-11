package io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.config.cooldown

import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.infrastructure.config.ConfigBase
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.dump.HeapDumpManager.Companion.DEFAULT_HEAP_DUMP_COOLDOWN
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CCooldowns : ConfigBase() {
    companion object {
        const val NAME = "cooldowns"
    }

    override val name: String = NAME

    val heapDump = s(
        "5m",
        "heap_dump",
        Comments.HEAP_DUMP
    ) { value ->
        val cooldown = value?.let {
            TimeFormatter.parseToDurationOrNull(it)
        } ?: return@s false
        cooldown in DEFAULT_HEAP_DUMP_COOLDOWN..Long.MAX_VALUE.minutes
    }

    val warning = s(
        "1m",
        "warning",
        Comments.WARNING
    ) { value ->
        val cooldown = value?.let {
            TimeFormatter.parseToDurationOrNull(it)
        } ?: return@s false
        cooldown in 10.seconds..Long.MAX_VALUE.seconds
    }
    val memoryReport = s(
        "3m",
        "memory_report",
        Comments.MEMORY_REPORT
    ) { value ->
        val cooldown = value?.let {
            TimeFormatter.parseToDurationOrNull(it)
        } ?: return@s false
        cooldown in 1.minutes..Long.MAX_VALUE.minutes
    }

    object Comments {
        const val COOLDOWNS =
            "The cooldown periods for various actions related to low memory conditions. These cooldowns help prevent excessive logging and reporting during low memory events."

        const val HEAP_DUMP =
            "The cooldown period between heap dump creations. Default and minimum is 5 minutes"
        const val WARNING =
            "The cooldown period between warning messages about low memory conditions. Default is 1 minute. Minimum is 10 seconds"
        const val MEMORY_REPORT =
            "The cooldown period between memory report generations. Default is 3 minutes. Minimum is 1 minute"
    }
}