package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.report

import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemoryReport
import java.nio.file.Path

interface MemoryReportWriter {
    fun write(output: Path, report: MemoryReport)
}