package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model

import io.conboi.oms.common.foundation.snapshot.Snapshot

data class MemorySnapshot(
    override val createdAt: Long,

    // Maximum heap size (-Xmx)
    val maxBytes: Long,

    // Memory currently allocated by the JVM from the OS
    val allocatedBytes: Long,

    // Free memory inside the allocated heap
    val freeAllocatedBytes: Long,
) : Snapshot {
    // Memory actually used by live objects
    val usedBytes: Long
        get() = allocatedBytes - freeAllocatedBytes

    // Memory still available before reaching -Xmx
    val availableBytes: Long
        get() = maxBytes - usedBytes

    // Percentage of used memory relative to the maximum heap size
    val usedPercent: Double
        get() = usedBytes.toDouble() / maxBytes * 100

    // Percentage of available memory relative to the maximum heap size
    val availablePercent: Double
        get() = availableBytes.toDouble() / maxBytes * 100
}