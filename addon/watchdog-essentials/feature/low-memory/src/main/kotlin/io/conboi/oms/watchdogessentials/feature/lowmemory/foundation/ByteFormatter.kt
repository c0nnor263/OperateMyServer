package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation

import java.util.Locale

object ByteFormatter {
    private const val KB = 1024.0
    private const val MB = KB * 1024
    private const val GB = MB * 1024

    fun format(bytes: Long): String {
        return when {
            bytes >= GB -> "%.2f GB".format(Locale.US, bytes / GB)
            bytes >= MB -> "%.1f MB".format(Locale.US, bytes / MB)
            bytes >= KB -> "%.1f KB".format(Locale.US, bytes / KB)
            else -> "$bytes B"
        }
    }
}
