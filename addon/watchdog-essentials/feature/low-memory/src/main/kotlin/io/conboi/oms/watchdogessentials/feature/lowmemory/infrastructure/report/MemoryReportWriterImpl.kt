package io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.report

import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.infrastructure.file.FileUtil
import io.conboi.oms.common.text.ComponentStyles.color
import io.conboi.oms.common.text.ComponentStyles.literal
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.ByteFormatter
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemoryReport
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.report.MemoryReportWriter
import java.nio.file.Path
import java.util.Locale
import net.minecraft.ChatFormatting


class MemoryReportWriterImpl : MemoryReportWriter {
    override fun write(output: Path, report: MemoryReport) {
        val content = buildString {
            appendLine("========================================")
            appendLine("OMS Watchdog Essentials")
            appendLine("Memory Report")
            appendLine("========================================")
            appendLine()
            appendLine("Created at:")
            appendLine("  ${TimeFormatter.formatDateTimeFileName(report.currentSnapshot?.createdAt ?: TimeHelper.currentEpochSeconds)}")
            appendLine()
            appendLine("Action:")
            appendLine("  ${report.action}")
            appendLine()
            appendLine("Configuration")
            appendLine("-------------")
            appendLine("Monitoring window : ${TimeFormatter.formatDuration(report.memoryCountTime)}")
            appendLine("Threshold         : ${report.thresholdPercent}%")
            appendLine()
            appendLine("Current Memory")
            appendLine("--------------")
            val currentSnapshot = report.currentSnapshot
            if (currentSnapshot == null) {
                append("No current memory snapshot available.\n".literal().color(ChatFormatting.YELLOW))
            } else {
                appendLine("Used             : ${ByteFormatter.format(currentSnapshot.usedBytes)}")
                appendLine("Available        : ${ByteFormatter.format(currentSnapshot.availableBytes)}")
                appendLine("Allocated        : ${ByteFormatter.format(currentSnapshot.allocatedBytes)}")
                appendLine("Max              : ${ByteFormatter.format(currentSnapshot.maxBytes)}")
                appendLine()
                appendLine("Used             : ${"%.1f".format(Locale.US, currentSnapshot.usedPercent)}%")
                appendLine("Available        : ${"%.1f".format(Locale.US, currentSnapshot.availablePercent)}%")
            }
            appendLine()
            appendLine("History")
            appendLine("-------")
            appendLine("Snapshots        : ${report.historySummary.snapshotsCount}")
            appendLine()
            appendLine("Average Used     : ${"%.1f".format(Locale.US, report.historySummary.averageUsedPercent)}%")
            appendLine("Average Available: ${"%.1f".format(Locale.US, report.historySummary.averageAvailablePercent)}%")
            appendLine()
            appendLine("Lowest Available : ${ByteFormatter.format(report.historySummary.minAvailableBytes)}")
            appendLine("Highest Used     : ${ByteFormatter.format(report.historySummary.maxUsedBytes)}")
        }

        FileUtil.writeSafe(output, content)
    }
}