package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation

import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemorySnapshot

object RuntimeMemoryProvider {
    fun snapshot(): MemorySnapshot {
        val now = TimeHelper.currentEpochSeconds
        val runtime: Runtime = Runtime.getRuntime()
        return MemorySnapshot(
            createdAt = now,
            maxBytes = runtime.maxMemory(),
            allocatedBytes = runtime.totalMemory(),
            freeAllocatedBytes = runtime.freeMemory()
        )
    }
}