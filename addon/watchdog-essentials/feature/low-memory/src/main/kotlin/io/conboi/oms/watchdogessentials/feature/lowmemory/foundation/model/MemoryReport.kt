package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model

import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.CriticalAction
import kotlin.time.Duration

data class MemoryReport(
    val action: CriticalAction,
    val currentSnapshot: MemorySnapshot?,
    val historySummary: RetentionSummary,
    val memoryCountTime: Duration,
    val thresholdPercent: Double
)