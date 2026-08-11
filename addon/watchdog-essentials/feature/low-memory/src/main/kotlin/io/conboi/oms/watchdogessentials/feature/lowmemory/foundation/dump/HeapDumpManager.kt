package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.dump

import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.infrastructure.file.FileUtil
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.dump.JvmHeapDumpWriter
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class HeapDumpManager(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val heapDumpWriter: JvmHeapDumpWriter = JvmHeapDumpWriter()
) {
    companion object {
        val DEFAULT_HEAP_DUMP_COOLDOWN: Duration = 5.minutes
    }

    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    private val isInProgress: AtomicBoolean = AtomicBoolean(false)
    private var nextAllowedEpochSeconds: Long = 0L

    private fun isCooldownActive(): Boolean =
        TimeHelper.currentEpochSeconds < nextAllowedEpochSeconds

    private fun isInProgress(): Boolean {
        if (!isInProgress.compareAndSet(false, true)) {
            LOG.warn("Heap dump creation is already in progress. Skipping this request.")
            return true
        }
        return false
    }

    fun createHeapDump(context: AddonContext, heapDumpCooldownDuration: Duration) {
        if (isCooldownActive()) return
        if (isInProgress()) return
        executeHeapDump(context = context, cooldownDuration = heapDumpCooldownDuration)
    }

    fun requestHeapDumpAsync(context: AddonContext) {
        scope.launch {
            executeHeapDump(context = context, cooldownDuration = 0.seconds)
        }
    }

    private fun executeHeapDump(context: AddonContext, cooldownDuration: Duration) {
        try {
            val path = writeHeapDump(context)
            if (cooldownDuration > 0.seconds) {
                nextAllowedEpochSeconds =
                    TimeHelper.currentEpochSeconds + cooldownDuration.inWholeSeconds
            }
            LOG.warn("Heap dump created: {}", path)
        } catch (error: IOException) {
            LOG.error("Failed to create heap dump due to an I/O error", error)
        } catch (error: UnsupportedOperationException) {
            LOG.error("Failed to create heap dump due to unsupported operation", error)
        } catch (error: SecurityException) {
            LOG.error("Failed to create heap dump due to security restrictions", error)
        } finally {
            isInProgress.set(false)
        }
    }

    private fun writeHeapDump(context: AddonContext): Path {
        val heapDumpDir = context.paths.addonRoot
            .resolve("low-memory")
            .resolve("heap-dumps")

        FileUtil.ensureDir(heapDumpDir)

        val time = TimeFormatter.formatDateTimeFileName(TimeHelper.currentEpochSeconds)
        val fileName = "heapdump-$time.hprof"
        val output = heapDumpDir.resolve(fileName)
        heapDumpWriter.write(output)
        return output
    }

    fun resetCooldown() {
        nextAllowedEpochSeconds = 0L
    }

    fun clear() {
        scope.cancel()
    }
}