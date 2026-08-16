package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.dump

import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.infrastructure.file.AddonPaths
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.infrastructure.file.FileUtil
import io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.dump.JvmHeapDumpWriter
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher

class HeapDumpManagerTest : ShouldSpec({

    class TestAddonPaths : AddonPaths("addon")

    lateinit var sut: HeapDumpManager
    lateinit var tempDir: Path
    lateinit var paths: TestAddonPaths

    val testDispatcher = StandardTestDispatcher()

    val context = mockk<AddonContext>()
    val writer = mockk<JvmHeapDumpWriter>()

    beforeSpec {
        mockkObject(TimeHelper)
        mockkObject(FileUtil)
    }

    beforeEach {
        tempDir = Files.createTempDirectory("heap_dump_manager_test")

        paths = TestAddonPaths().apply {
            onInitializeOmsRoot(tempDir)
        }

        every { context.paths } returns paths
        every { TimeHelper.currentEpochSeconds } returns 1_000L
        every { FileUtil.ensureDir(any()) } returns Unit
        every { writer.write(any()) } just Runs

        sut = HeapDumpManager(dispatcher = testDispatcher, heapDumpWriter = writer)
    }

    afterEach {
        sut.clear()
        testDispatcher.cancel()
        tempDir.toFile().deleteRecursively()
        clearAllMocks()
    }

    context("createHeapDump") {

        should("create a heap dump") {
            val pathSlot = slot<Path>()
            every { writer.write(capture(pathSlot)) } just Runs

            sut.createHeapDump(
                context = context,
                heapDumpCooldownDuration = 5.minutes,
            )

            verify(exactly = 1) { writer.write(any()) }
            pathSlot.captured.parent shouldBe paths.addonRoot
                .resolve("low-memory")
                .resolve("heap-dumps")
            pathSlot.captured.fileName.toString().startsWith("heapdump-") shouldBe true
            pathSlot.captured.fileName.toString().endsWith(".hprof") shouldBe true
        }

        should("rotate old heap dumps and keep only the newest files") {
            val rotatingWriter = object : HeapDumpWriter {
                override fun write(output: Path) {
                    Files.createDirectories(output.parent)
                    Files.writeString(output, "heap dump")
                }
            }
            val rotatingSut = HeapDumpManager(
                dispatcher = testDispatcher,
                heapDumpWriter = rotatingWriter,
                maxRetainedHeapDumps = 2,
            )

            rotatingSut.createHeapDump(context, 0.minutes)
            rotatingSut.createHeapDump(context, 0.minutes)
            rotatingSut.createHeapDump(context, 0.minutes)

            val heapDumpDir = paths.addonRoot.resolve("low-memory").resolve("heap-dumps")
            val timestamp = TimeFormatter.formatDateTimeFileName(1_000L)
            heapDumpDir.toFile().listFiles()?.map { it.name }?.sorted() shouldBe listOf(
                "heapdump-$timestamp-2.hprof",
                "heapdump-$timestamp-3.hprof",
            )
        }

        should("generate unique heap dump names within the same second") {
            val capturedPaths = mutableListOf<Path>()
            every { writer.write(capture(capturedPaths)) } just Runs

            sut.createHeapDump(context, 0.minutes)
            sut.resetCooldown()
            sut.createHeapDump(context, 0.minutes)

            capturedPaths.size shouldBe 2
            capturedPaths[0].fileName.toString().startsWith("heapdump-") shouldBe true
            capturedPaths[1].fileName.toString().startsWith("heapdump-") shouldBe true
            capturedPaths[0].fileName.toString().endsWith("-1.hprof") shouldBe true
            capturedPaths[1].fileName.toString().endsWith("-2.hprof") shouldBe true
            capturedPaths[0].fileName.toString() shouldBe capturedPaths[1].fileName.toString().replace("-2.hprof", "-1.hprof")
        }

        should("skip a heap dump while cooldown is active") {
            sut.createHeapDump(context, 5.minutes)
            sut.createHeapDump(context, 5.minutes)

            verify(exactly = 1) {
                writer.write(any())
            }
        }

        should("allow a heap dump when cooldown expires") {
            every { TimeHelper.currentEpochSeconds } returns 1_000L

            sut.createHeapDump(context, 5.minutes)

            every { TimeHelper.currentEpochSeconds } returns 1_299L
            sut.createHeapDump(context, 5.minutes)

            every { TimeHelper.currentEpochSeconds } returns 1_300L
            sut.createHeapDump(context, 5.minutes)

            verify(exactly = 2) {
                writer.write(any())
            }
        }

        should("allow a heap dump after cooldown reset") {
            sut.createHeapDump(context, 5.minutes)
            sut.createHeapDump(context, 5.minutes)

            sut.resetCooldown()

            sut.createHeapDump(context, 5.minutes)

            verify(exactly = 2) {
                writer.write(any())
            }
        }

        should("skip a request when heap dump creation is already in progress") {
            val writingStarted = CountDownLatch(1)
            val allowWritingToFinish = CountDownLatch(1)
            val executor = Executors.newSingleThreadExecutor()

            every { writer.write(any()) } answers {
                writingStarted.countDown()

                check(
                    allowWritingToFinish.await(
                        5,
                        TimeUnit.SECONDS,
                    ),
                ) {
                    "Timed out while waiting to finish heap dump writing"
                }
            }

            try {
                val firstRequest = executor.submit {
                    sut.createHeapDump(
                        context = context,
                        heapDumpCooldownDuration = Duration.ZERO,
                    )
                }

                check(
                    writingStarted.await(
                        5,
                        TimeUnit.SECONDS,
                    ),
                ) {
                    "Heap dump writing did not start"
                }

                sut.createHeapDump(
                    context = context,
                    heapDumpCooldownDuration = Duration.ZERO,
                )

                verify(exactly = 1) {
                    writer.write(any())
                }

                allowWritingToFinish.countDown()
                firstRequest.get(5, TimeUnit.SECONDS)
            } finally {
                allowWritingToFinish.countDown()
                executor.shutdownNow()
            }
        }

        context("exception handling") {

            should("release in-progress state after an I/O error") {
                every { writer.write(any()) } throws IOException("Test error")

                sut.createHeapDump(
                    context = context,
                    heapDumpCooldownDuration = Duration.ZERO,
                )

                every { writer.write(any()) } just Runs

                sut.createHeapDump(
                    context = context,
                    heapDumpCooldownDuration = Duration.ZERO,
                )

                verify(exactly = 2) {
                    writer.write(any())
                }
            }

            should("release in-progress state after an UnsupportedOperationException error") {
                every { writer.write(any()) } throws UnsupportedOperationException("Test error")

                sut.createHeapDump(
                    context = context,
                    heapDumpCooldownDuration = Duration.ZERO,
                )

                every { writer.write(any()) } just Runs

                sut.createHeapDump(
                    context = context,
                    heapDumpCooldownDuration = Duration.ZERO,
                )

                verify(exactly = 2) {
                    writer.write(any())
                }
            }

            should("release in-progress state after an SecurityException error") {
                every { writer.write(any()) } throws SecurityException("Test error")

                sut.createHeapDump(
                    context = context,
                    heapDumpCooldownDuration = Duration.ZERO,
                )

                every { writer.write(any()) } just Runs

                sut.createHeapDump(
                    context = context,
                    heapDumpCooldownDuration = Duration.ZERO,
                )

                verify(exactly = 2) {
                    writer.write(any())
                }
            }

            should("release in-progress state after a runtime error") {
                every { writer.write(any()) } throws IllegalStateException("Test error")

                sut.createHeapDump(
                    context = context,
                    heapDumpCooldownDuration = Duration.ZERO,
                )

                every { writer.write(any()) } just Runs

                sut.createHeapDump(
                    context = context,
                    heapDumpCooldownDuration = Duration.ZERO,
                )

                verify(exactly = 2) {
                    writer.write(any())
                }
            }
        }

        should("not apply cooldown when heap dump creation fails") {
            every { writer.write(any()) } throws IOException("Test error")

            sut.createHeapDump(
                context = context,
                heapDumpCooldownDuration = 5.minutes,
            )

            every { writer.write(any()) } just Runs

            sut.createHeapDump(
                context = context,
                heapDumpCooldownDuration = 5.minutes,
            )

            verify(exactly = 2) {
                writer.write(any())
            }
        }
    }

    context("requestHeapDumpAsync") {
        should("create a heap dump asynchronously") {
            val pathSlot = slot<Path>()
            every { writer.write(capture(pathSlot)) } just Runs

            sut.requestHeapDumpAsync(context)

            testDispatcher.scheduler.advanceUntilIdle()

            verify(exactly = 1) { writer.write(any()) }
            pathSlot.captured.parent shouldBe paths.addonRoot
                .resolve("low-memory")
                .resolve("heap-dumps")
            pathSlot.captured.fileName.toString().startsWith("heapdump-") shouldBe true
            pathSlot.captured.fileName.toString().endsWith(".hprof") shouldBe true
        }
    }
})