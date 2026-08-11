package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model

import io.conboi.oms.common.foundation.snapshot.SnapshotHistory
import kotlin.time.Duration

data class RetentionSummary(
    override val snapshotsCount: Int,
    val retentionWindow: Duration,
    val averageUsedBytes: Long,
    val averageAvailableBytes: Long,
    val averageUsedPercent: Double,
    val averageAvailablePercent: Double,
    val minAvailableBytes: Long,
    val maxUsedBytes: Long
): SnapshotHistory.Summary