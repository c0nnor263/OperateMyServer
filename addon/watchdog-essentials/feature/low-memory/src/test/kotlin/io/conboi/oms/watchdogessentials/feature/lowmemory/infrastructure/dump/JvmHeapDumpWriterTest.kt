package io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.dump

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path

class JvmHeapDumpWriterTest : ShouldSpec({

    lateinit var tempDir: Path

    beforeEach {
        tempDir = Files.createTempDirectory("heap_dump_writer_test")
    }

    afterEach {
        tempDir.toFile().deleteRecursively()
    }

    context("write") {
        should("create a heap dump file at the output path") {
            val output = tempDir.resolve("dump.hprof")

            JvmHeapDumpWriter().write(output)

            output.toFile().exists() shouldBe true
        }
    }
})
