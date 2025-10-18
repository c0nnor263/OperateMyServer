package io.conboi.oms.addon.bundled.scheduledrestart.content

import java.time.ZonedDateTime

internal sealed class SkipResult {
    data class Skipped(val skipped: ZonedDateTime, val next: ZonedDateTime) : SkipResult()
    data class AlreadySkipped(val next: ZonedDateTime) : SkipResult()
}