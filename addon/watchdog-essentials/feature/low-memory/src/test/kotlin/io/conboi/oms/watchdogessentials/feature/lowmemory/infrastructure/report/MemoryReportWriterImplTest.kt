package io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.report

import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.CriticalAction
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemoryReport
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemorySnapshot
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.RetentionSummary
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Duration.Companion.minutes

class MemoryReportWriterImplTest : ShouldSpec({

    lateinit var tempDir: Path

    beforeSpec {
        mockkObject(TimeHelper)
    }

    beforeEach {
        tempDir = Files.createTempDirectory("memory_report_writer_test")
        every { TimeHelper.currentEpochSeconds } returns 1_234L
    }

    afterEach {
        tempDir.toFile().deleteRecursively()
        clearAllMocks()
    }

    context("write") {
        should("write report without current snapshot") {
            val output = tempDir.resolve("report.log")
            val report = report(currentSnapshot = null)
            val expectedCreatedAt = TimeFormatter.formatDateTimeFileName(1_234L)

            MemoryReportWriterImpl().write(output, report)

            val content = output.toFile().readText()
            content shouldContain "Created at:"
            content shouldContain expectedCreatedAt
            content shouldContain "No current memory snapshot available."
            content shouldContain "Monitoring window : 5m"
            content shouldContain "Threshold         : 10.0%"
            content shouldContain "Snapshots        : 3"
            content shouldContain "Average Used     : 1.0%"
            content shouldContain "Average Available: 2.0%"
        }

        should("write current memory and history values") {
            val output = tempDir.resolve("report.log")
            val report = report(
                currentSnapshot = MemorySnapshot(
                    createdAt = 1L,
                    maxBytes = 8_192L,
                    allocatedBytes = 4_096L,
                    freeAllocatedBytes = 2_048L
                )
            )

            MemoryReportWriterImpl().write(output, report)

            val content = output.toFile().readText()
            content shouldContain "Used             : 2.0 KB"
            content shouldContain "Available        : 6.0 KB"
            content shouldContain "Allocated        : 4.0 KB"
            content shouldContain "Max              : 8.0 KB"
            content shouldContain "Used             : 25.0%"
            content shouldContain "Available        : 75.0%"
            content shouldContain "Lowest Available : 2.0 KB"
            content shouldContain "Highest Used     : 2.0 KB"
        }
    }
})

private fun report(currentSnapshot: MemorySnapshot?): MemoryReport {
    return MemoryReport(
        action = CriticalAction.RESTART,
        currentSnapshot = currentSnapshot,
        historySummary = RetentionSummary(
            snapshotsCount = 3,
            retentionWindow = 5.minutes,
            averageUsedBytes = 1L,
            averageAvailableBytes = 2L,
            averageUsedPercent = 1.0,
            averageAvailablePercent = 2.0,
            minAvailableBytes = 2_048L,
            maxUsedBytes = 2_048L
        ),
        memoryCountTime = 5.minutes,
        thresholdPercent = 10.0
    )
}
