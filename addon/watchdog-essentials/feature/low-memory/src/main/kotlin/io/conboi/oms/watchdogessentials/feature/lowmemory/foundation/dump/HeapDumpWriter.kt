package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.dump

import java.io.IOException
import java.nio.file.Path

interface HeapDumpWriter {
    @Throws(IOException::class, UnsupportedOperationException::class, SecurityException::class)
    fun write(output: Path)
}