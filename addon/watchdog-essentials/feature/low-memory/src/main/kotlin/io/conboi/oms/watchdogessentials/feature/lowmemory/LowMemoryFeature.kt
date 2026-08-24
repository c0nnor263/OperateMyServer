package io.conboi.oms.watchdogessentials.feature.lowmemory

import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.TickTimer
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.cachedField
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.api.permission.PermissionLevel
import io.conboi.oms.api.permission.hasPermission
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.infrastructure.lang.OmsLang
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import io.conboi.oms.watchdogessentials.feature.lowmemory.elements.commands.dump.HeapDumpCommand
import io.conboi.oms.watchdogessentials.feature.lowmemory.elements.commands.report.MemoryReportCommand
import io.conboi.oms.watchdogessentials.feature.lowmemory.elements.commands.summary.SummaryCommand
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.ByteFormatter
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.CriticalAction
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.MemorySnapshotHistory
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.MemoryUnit
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.RuntimeMemoryProvider
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.dump.HeapDumpManager
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemoryReport
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemorySnapshot
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.RetentionSummary
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.reason.LowMemoryStop
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.report.MemoryReportManager
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.report.MemoryReportManager.Companion.DEFAULT_MEMORY_REPORT_COOLDOWN
import io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.config.CLowMemoryFeature
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class LowMemoryFeature(
    configProvider: ConfigProvider<CLowMemoryFeature>
) : OmsFeature<CLowMemoryFeature>(configProvider) {
    companion object {
        // Internal check interval for memory monitoring and action dispatch.
        val TICK_TIMER_INTERVAL = 15.seconds
        val RECOMMENDED_SERVER_MAX_MEMORY = MemoryUnit.gb(2)

        val DEFAULT_WARNING_COOLDOWN = 60.seconds
    }

    val averagingWindow: Duration by configField {
        key = { config.averagingWindow.get() }
        value = {
            TimeFormatter.parseToDurationOrNull(config.averagingWindow.get())
                ?: error("Cannot parse averagingWindow")
        }
    }

    val availableThresholdPercent: Double by configField {
        key = { config.availableThresholdPercent.get() }
        value = { config.availableThresholdPercent.get() }
    }

    val criticalAction: CriticalAction by configField {
        key = { config.criticalAction.get() }
        value = { CriticalAction.valueOf(config.criticalAction.get()) }
    }

    val createHeapDumpOnAction: Boolean by configField {
        key = { config.createHeapDumpOnAction.get() }
        value = { config.createHeapDumpOnAction.get() }
    }

    val warningCooldownDuration: Duration by configField {
        key = { config.cooldowns.warning.get() }
        value = {
            TimeFormatter.parseToDurationOrNull(config.cooldowns.warning.get())
                ?: DEFAULT_WARNING_COOLDOWN
        }
        onUpdate = { _, _ ->
            nextWarningAllowedEpochSeconds = 0L
        }
    }

    val memoryReportCooldownDuration: Duration by configField {
        key = { config.cooldowns.memoryReport.get() }
        value = {
            TimeFormatter.parseToDurationOrNull(config.cooldowns.memoryReport.get())
                ?: DEFAULT_MEMORY_REPORT_COOLDOWN
        }
        onUpdate = { _, _ ->
            memoryReportManager.resetCooldown()
        }
    }

    val heapDumpCooldownDuration: Duration by configField {
        key = { config.cooldowns.heapDump.get() }
        value = {
            TimeFormatter.parseToDurationOrNull(config.cooldowns.heapDump.get())
                ?: HeapDumpManager.DEFAULT_HEAP_DUMP_COOLDOWN
        }
        onUpdate = { _, _ ->
            heapDumpManager.resetCooldown()
        }
    }

    val retentionHeapDump: Int by configField {
        key = { config.retentions.heapDump.get() }
        value = { config.retentions.heapDump.get() }
    }

    val retentionMemoryReport: Int by configField {
        key = { config.retentions.memoryReport.get() }
        value = { config.retentions.memoryReport.get() }
    }

    val memorySnapshotHistory: MemorySnapshotHistory by cachedField {
        key = { averagingWindow }
        value = {
            MemorySnapshotHistory(retentionWindow = averagingWindow, checkInterval = TICK_TIMER_INTERVAL)
        }
    }

    val heapDumpManager: HeapDumpManager by cachedField {
        key = { retentionHeapDump }
        value = { HeapDumpManager(maxRetainedHeapDumps = retentionHeapDump) }
    }
    val memoryReportManager: MemoryReportManager by cachedField {
        key = { retentionMemoryReport }
        value = { MemoryReportManager(maxRetainedReports = retentionMemoryReport) }
    }


    override fun info(): FeatureInfo {
        return super.info().copy(
            id = CLowMemoryFeature.NAME,
            priority = Priority.CRITICAL,
        )
    }

    override val additionalCommands: List<OMSCommandEntry> = listOf(
        HeapDumpCommand(this),
        SummaryCommand(this),
        MemoryReportCommand(this)
    )

    private val tickTimer: TickTimer = TickTimer(TICK_TIMER_INTERVAL.inWholeSeconds.toInt() * 20)

    private var stopRequested: Boolean = false
    private var heapDumpRequested: Boolean = false
    private var memoryReportRequested: Boolean = false
    private var nextWarningAllowedEpochSeconds: Long = 0L

    override fun onOmsStarted(event: OMSLifecycle.StartingEvent, context: AddonContext) {
        super.onOmsStarted(event, context)
        if (!config.startupCheck.get()) return

        val snapshot = RuntimeMemoryProvider.snapshot()
        if (snapshot.maxBytes < RECOMMENDED_SERVER_MAX_MEMORY) {
            LOG.warn(
                "OMS detected that the server is running with less than ${
                    ByteFormatter.format(
                        RECOMMENDED_SERVER_MAX_MEMORY
                    )
                } max JVM memory.\n" +
                        "This may be enough for small or test servers, but is not recommended for production Minecraft servers"
            )
        }
    }

    override fun onOmsTick(event: OMSLifecycle.TickingEvent, context: AddonContext) {
        checkForRequests(context)

        val server = event.server
        if (!tickTimer.shouldFire(server.tickCount)) return
        super.onOmsTick(event, context)

        val snapshot = RuntimeMemoryProvider.snapshot()
        memorySnapshotHistory.add(snapshot)
        val averageAvailableMemoryPercent = memorySnapshotHistory.averageAvailablePercent()
        if (!stopRequested && averageAvailableMemoryPercent < availableThresholdPercent) {
            LOG.warn(
                "Low memory detected (avg available=${
                    "%.1f".format(
                        Locale.US,
                        averageAvailableMemoryPercent
                    )
                }%) for ${averagingWindow}, threshold is ${availableThresholdPercent}%"
            )
            tryCreateMemoryReport(context = context, isRequest = false)
            tryCreateHeapDump(context = context, isRequest = false)
            performCriticalAction(server = server, averageAvailableMemoryPercent = averageAvailableMemoryPercent)
        }
    }

    override fun onOmsStopping(event: OMSLifecycle.StoppingEvent, context: AddonContext) {
        super.onOmsStopping(event, context)
        heapDumpManager.clear()
        memoryReportManager.clear()
    }

    private fun checkForRequests(context: AddonContext) {
        if (heapDumpRequested) {
            tryCreateHeapDump(context = context, isRequest = true)
            heapDumpRequested = false
        }

        if (memoryReportRequested) {
            tryCreateMemoryReport(context = context, isRequest = true)
            memoryReportRequested = false
        }
    }

    private fun tryCreateHeapDump(context: AddonContext, isRequest: Boolean) {
        if (isRequest) {
            heapDumpManager.requestHeapDumpAsync(context)
            return
        }
        if (!createHeapDumpOnAction) return

        heapDumpManager.createHeapDump(context, heapDumpCooldownDuration)
    }

    private fun tryCreateMemoryReport(context: AddonContext, isRequest: Boolean) {
        val report = MemoryReport(
            action = criticalAction,
            currentSnapshot = memorySnapshotHistory.latest(),
            historySummary = memorySnapshotHistory.summaryOver(averagingWindow),
            memoryCountTime = averagingWindow,
            thresholdPercent = availableThresholdPercent
        )
        if (isRequest) {
            memoryReportManager.requestReportAsync(context, report)
        } else {
            memoryReportManager.createReport(context, report, memoryReportCooldownDuration)
        }
    }

    private fun performCriticalAction(server: MinecraftServer, averageAvailableMemoryPercent: Double) {
        val args = LowMemoryStop.Arguments(
            averageAvailableMemoryPercent = "%.1f".format(Locale.US, averageAvailableMemoryPercent),
            memoryCountTime = TimeFormatter.formatDuration(averagingWindow),
            memoryAvailableThresholdPercent = "%.1f".format(Locale.US, availableThresholdPercent)
        )
        when (val action = criticalAction) {
            CriticalAction.WARNING -> {
                val now = TimeHelper.currentEpochSeconds
                if (now < nextWarningAllowedEpochSeconds) {
                    return
                }
                nextWarningAllowedEpochSeconds = now + warningCooldownDuration.inWholeSeconds
                server.playerList.players
                    .filter { it.hasPermission(PermissionLevel.OWNER) }
                    .forEach {
                        it.sendSystemMessage(
                            OmsLang.translatable(
                                "watchdogessentials.warning.low_memory",
                                *args.toArray()
                            ),
                            false
                        )
                    }
            }

            else -> {
                stopRequested = true
                val stopReason = LowMemoryStop.fromCriticalAction(action, args)
                val event = OMSActions.StopRequestedEvent(
                    server,
                    stopReason
                )
                FORGE_BUS.post(event)
            }
        }
    }

    fun requestHeapDump() {
        heapDumpRequested = true
    }

    fun requestReport() {
        memoryReportRequested = true
    }

    fun requestSummary(): MemorySnapshot? {
        return memorySnapshotHistory.latest()
    }

    fun requestSummaryOver(window: Duration): RetentionSummary {
        return memorySnapshotHistory.summaryOver(window)
    }
}