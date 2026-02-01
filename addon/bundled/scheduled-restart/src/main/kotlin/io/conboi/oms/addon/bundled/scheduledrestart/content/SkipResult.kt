package io.conboi.oms.addon.bundled.scheduledrestart.content

sealed class SkipResult {
    data class Skipped(val skippedRestartTime: Long, val nextRestartTime: Long) : SkipResult()
    data class AlreadySkipped(val nextRestartTime: Long) : SkipResult()
}