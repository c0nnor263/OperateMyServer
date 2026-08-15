package io.conboi.oms.watchdogessentials.feature.lowtps.foundation

import io.conboi.oms.common.foundation.snapshot.Snapshot

data class TpsSnapshot(
    override val createdAt: Long,
    val value: Double
) : Snapshot