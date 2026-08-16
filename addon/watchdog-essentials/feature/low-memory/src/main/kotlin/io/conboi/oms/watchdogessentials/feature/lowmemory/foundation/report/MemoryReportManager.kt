package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.report

import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.infrastructure.file.FileUtil
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemoryReport
import io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.report.MemoryReportWriterImpl
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.name
import kotlin.streams.asSequence
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MemoryReportManager(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val reportWriter: MemoryReportWriter = MemoryReportWriterImpl(),
    private val maxRetainedReports: Int
) {
    companion object {
        val DEFAULT_MEMORY_REPORT_COOLDOWN = 3.minutes
    }

    init {
        require(maxRetainedReports > 0) { "maxRetainedReports must be greater than 0" }
    }

    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    private val isInProgress: AtomicBoolean = AtomicBoolean(false)
    private val fileSequence = AtomicLong(0L)
    private var nextAllowedEpochSeconds: Long = 0L


    private fun isCooldownActive(): Boolean =
        TimeHelper.currentEpochSeconds < nextAllowedEpochSeconds

    private fun isInProgress(): Boolean {
        if (!isInProgress.compareAndSet(false, true)) {
            LOG.warn("Memory Report creation is already in progress. Skipping this request.")
            return true
        }
        return false
    }

    fun createReport(context: AddonContext, report: MemoryReport, cooldownDuration: Duration) {
        if (isCooldownActive()) return
        if (isInProgress()) return
        executeMemoryReportCreation(
            context = context,
            report = report,
            cooldownDuration = cooldownDuration
        )
    }

    fun requestReportAsync(context: AddonContext, report: MemoryReport) {
        scope.launch {
            executeMemoryReportCreation(context = context, report = report, cooldownDuration = 0.seconds)
        }
    }

    private fun executeMemoryReportCreation(
        context: AddonContext,
        report: MemoryReport,
        cooldownDuration: Duration
    ) {
        try {
            val path = writeReport(context, report = report)
            if (cooldownDuration > 0.seconds) {
                nextAllowedEpochSeconds =
                    TimeHelper.currentEpochSeconds + cooldownDuration.inWholeSeconds
            }
            LOG.warn("Report created: {}", path)
        } catch (error: IOException) {
            LOG.error("Failed to create report due to an I/O error", error)
        } catch (error: UnsupportedOperationException) {
            LOG.error("Failed to create report due to unsupported operation", error)
        } catch (error: SecurityException) {
            LOG.error("Failed to create report due to security restrictions", error)
        } catch (error: RuntimeException) {
            LOG.error("Failed to create report", error)
        } finally {
            isInProgress.set(false)
        }
    }

    private fun writeReport(context: AddonContext, report: MemoryReport): Path {
        val reportsDir = context.paths.addonRoot
            .resolve("low-memory")
            .resolve("reports")

        FileUtil.ensureDir(reportsDir)

        val time = TimeFormatter.formatDateTimeFileName(TimeHelper.currentEpochSeconds)
        val fileName = "memory-report-$time-${fileSequence.incrementAndGet()}.log"
        val output = reportsDir.resolve(fileName)

        reportWriter.write(output, report)
        pruneOldReports(reportsDir)
        return output
    }

    private fun pruneOldReports(reportsDir: Path) {
        try {
            val reportFiles = Files.list(reportsDir).use { stream ->
                stream.asSequence()
                    .filter { Files.isRegularFile(it) }
                    .filter {
                        val fileName = it.name
                        fileName.startsWith("memory-report-") && fileName.endsWith(".log")
                    }
                    .sortedBy { it.name }
                    .toList()
            }

            val filesToDelete = reportFiles.size - maxRetainedReports
            if (filesToDelete <= 0) return

            reportFiles
                .take(filesToDelete)
                .forEach { Files.deleteIfExists(it) }
        } catch (error: IOException) {
            LOG.warn("Failed to rotate memory report files", error)
        }
    }

    fun resetCooldown() {
        nextAllowedEpochSeconds = 0L
    }

    fun clear() {
        scope.cancel()
    }
}