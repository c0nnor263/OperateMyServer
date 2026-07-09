package io.conboi.oms.feature.scheduledrestart.foundation

sealed class SkipResult {
    data class Skipped(val skippedRestartTime: Long, val nextRestartTime: Long) : SkipResult()
    data class AlreadySkipped(val nextRestartTime: Long) : SkipResult()
}