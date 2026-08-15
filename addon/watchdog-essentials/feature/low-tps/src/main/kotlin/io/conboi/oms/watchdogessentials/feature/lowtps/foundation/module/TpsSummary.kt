package io.conboi.oms.watchdogessentials.feature.lowtps.foundation.module

import io.conboi.oms.common.foundation.snapshot.SnapshotHistory

data class TpsSummary(override val snapshotsCount: Int) : SnapshotHistory.Summary
