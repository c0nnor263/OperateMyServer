package io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.config.retention

import io.conboi.oms.common.infrastructure.config.ConfigBase

class CRetentions : ConfigBase() {
    companion object {
        const val NAME = "retentions"
    }

    override val name: String = NAME

    val heapDump = i(
        3,
        1,
        10,
        "heap_dump",
        Comments.HEAP_DUMP
    )

    val memoryReport = i(
        5,
        1,
        20,
        "memory_report",
        Comments.MEMORY_REPORT
    )

    object Comments {
        const val RETENTIONS = "Configuration for retention periods of low memory file generations"
        
        const val HEAP_DUMP = "The retention period for heap dump files. Default is 3 files. Minimum is 1 file. Maximum is 10 files."
        const val MEMORY_REPORT = "The retention period for memory report files. Default is 5 files. Minimum is 1 file. Maximum is 20 files."
    }
}