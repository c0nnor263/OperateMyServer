package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation

object MemoryUnit {
    const val KB = 1024L
    const val MB = KB * 1024
    const val GB = MB * 1024

    fun mb(value: Long): Long = value * MB
    fun gb(value: Long): Long = value * GB
}