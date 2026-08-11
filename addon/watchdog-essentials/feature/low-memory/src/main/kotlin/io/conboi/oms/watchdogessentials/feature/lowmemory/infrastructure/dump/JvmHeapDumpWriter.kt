package io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.dump

import com.sun.management.HotSpotDiagnosticMXBean
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.dump.HeapDumpWriter
import java.lang.management.ManagementFactory
import java.nio.file.Path

class JvmHeapDumpWriter : HeapDumpWriter {
    override fun write(output: Path) {
        val mxBean = ManagementFactory
            .getPlatformMXBean(HotSpotDiagnosticMXBean::class.java)
        mxBean.dumpHeap(output.toAbsolutePath().toString(), true)
    }
}