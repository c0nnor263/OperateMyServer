package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation

import io.conboi.oms.common.foundation.snapshot.SnapshotHistory
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemorySnapshot
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.RetentionSummary
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class MemorySnapshotHistory(
    retentionWindow: Duration,
    checkInterval: Duration
) : SnapshotHistory<MemorySnapshot, RetentionSummary>(
    retentionWindow = retentionWindow,
    checkInterval = checkInterval
) {
    companion object {
        val MIN_RETENTION_MINUTES = 1.minutes
        val MAX_RETENTION_MINUTES = 10.minutes
        const val DEFAULT_MEMORY_AVAILABLE_PERCENT = 100.0
    }

    override fun summary(): RetentionSummary {
        return createSummaryFromSnapshots(history)
    }

    override fun summaryOver(window: Duration): RetentionSummary {
        val filteredSnapshotList = filteredHistoryOver(window)
        return createSummaryFromSnapshots(filteredSnapshotList, window)
    }

    fun averageAvailablePercent(): Double {
        val snapshots = filteredHistoryOver(retentionWindow)
        if (snapshots.isEmpty()) return DEFAULT_MEMORY_AVAILABLE_PERCENT

        return averageOf(snapshots) { it.availablePercent }
    }

    private fun createSummaryFromSnapshots(
        snapshots: List<MemorySnapshot>,
        window: Duration = retentionWindow
    ): RetentionSummary {
        return RetentionSummary(
            snapshotsCount = snapshots.size,
            retentionWindow = window,
            averageUsedBytes = averageOf(snapshots) { it.usedBytes.toDouble() }.toLong(),
            averageAvailableBytes = averageOf(snapshots) { it.availableBytes.toDouble() }.toLong(),
            averageUsedPercent = averageOf(snapshots) { it.usedPercent },
            averageAvailablePercent = averageOf(snapshots) { it.availablePercent },
            minAvailableBytes = snapshots.minOfOrNull { it.availableBytes } ?: 0L,
            maxUsedBytes = snapshots.maxOfOrNull { it.usedBytes } ?: 0L
        )
    }
}