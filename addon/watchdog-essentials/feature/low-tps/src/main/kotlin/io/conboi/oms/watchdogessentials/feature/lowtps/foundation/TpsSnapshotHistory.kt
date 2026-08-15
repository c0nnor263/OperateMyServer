package io.conboi.oms.watchdogessentials.feature.lowtps.foundation

import io.conboi.oms.common.foundation.snapshot.SnapshotHistory
import io.conboi.oms.watchdogessentials.feature.lowtps.foundation.module.TpsSummary
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class TpsSnapshotHistory(
    retentionWindow: Duration,
    checkInterval: Duration
) : SnapshotHistory<TpsSnapshot, TpsSummary>(
    retentionWindow = retentionWindow,
    checkInterval = checkInterval
) {
    companion object {
        val MIN_RETENTION_MINUTES = 1.minutes
        val MAX_RETENTION_MINUTES = 5.minutes
        const val DEFAULT_TPS = 20.0
    }

    override fun summary(): TpsSummary {
        return createSummaryFromSnapshots(history)
    }

    override fun summaryOver(window: Duration): TpsSummary {
        val filteredSnapshotList = filteredHistoryOver(window)
        return createSummaryFromSnapshots(filteredSnapshotList, window)
    }

    private fun createSummaryFromSnapshots(
        snapshots: List<TpsSnapshot>,
        window: Duration = retentionWindow
    ): TpsSummary {
        return TpsSummary(snapshotsCount = snapshots.size)
    }

    fun averageTps(): Double {
        val snapshots = filteredHistoryOver(retentionWindow)
        // Ensure we have enough data points and the requested duration is reasonable
        if (snapshots.isEmpty()) return DEFAULT_TPS

        return averageOf(snapshots) { it.value }
    }
}