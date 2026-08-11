package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.report

import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.infrastructure.file.AddonPaths
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.CriticalAction
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemoryReport
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemorySnapshot
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.RetentionSummary
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
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.StandardTestDispatcher

class MemoryReportManagerTest : ShouldSpec({

    class TestAddonPaths : AddonPaths("addon")

    lateinit var sut: MemoryReportManager
    lateinit var tempDir: Path
    lateinit var paths: TestAddonPaths

    val testDispatcher = StandardTestDispatcher()

    val mockContext = mockk<AddonContext>(relaxed = true)
    val mockWriter = mockk<MemoryReportWriter>(relaxed = true)

    beforeSpec {
        mockkObject(io.conboi.oms.common.foundation.TimeHelper)
        mockkObject(io.conboi.oms.common.infrastructure.file.FileUtil)
    }

    beforeEach {
        tempDir = Files.createTempDirectory("memory_report_manager_test")
        paths = TestAddonPaths().apply { onInitializeOmsRoot(tempDir) }
        every { io.conboi.oms.common.foundation.TimeHelper.currentEpochSeconds } returns 1_000L
        every { mockContext.paths } returns paths

        sut = MemoryReportManager(dispatcher = testDispatcher, reportWriter = mockWriter)
    }

    afterEach {
        sut.clear()
        tempDir.toFile().deleteRecursively()
        clearAllMocks()
    }

    context("createReport") {
        should("write report and apply cooldown") {
            val report = report()
            val pathSlot = slot<Path>()
            every { mockWriter.write(capture(pathSlot), report) } just Runs

            sut.createReport(mockContext, report, 3.minutes)
            sut.createReport(mockContext, report, 3.minutes)
            sut.resetCooldown()
            sut.createReport(mockContext, report, 3.minutes)

            verify(exactly = 2) { mockWriter.write(any(), report) }
            pathSlot.captured.parent shouldBe tempDir
                .resolve("addon")
                .resolve("low-memory")
                .resolve("reports")
        }

        should("skip a report while cooldown is active") {
            val report = report()

            sut.createReport(mockContext, report, 3.minutes)
            sut.createReport(mockContext, report, 3.minutes)

            verify(exactly = 1) { mockWriter.write(any(), report) }
        }

        should("allow a report when cooldown expires") {
            val report = report()
            every { io.conboi.oms.common.foundation.TimeHelper.currentEpochSeconds } returns 1_000L

            sut.createReport(mockContext, report, 5.minutes)

            every { io.conboi.oms.common.foundation.TimeHelper.currentEpochSeconds } returns 1_299L
            sut.createReport(mockContext, report, 5.minutes)

            every { io.conboi.oms.common.foundation.TimeHelper.currentEpochSeconds } returns 1_300L
            sut.createReport(mockContext, report, 5.minutes)

            verify(exactly = 2) { mockWriter.write(any(), report) }
        }

        should("allow a report after cooldown reset") {
            val report = report()

            sut.createReport(mockContext, report, 3.minutes)
            sut.createReport(mockContext, report, 3.minutes)

            sut.resetCooldown()

            sut.createReport(mockContext, report, 3.minutes)

            verify(exactly = 2) { mockWriter.write(any(), report) }
        }

        should("skip a request when report creation is already in progress") {
            val report = report()
            val writingStarted = CountDownLatch(1)
            val allowWritingToFinish = CountDownLatch(1)
            val executor = Executors.newSingleThreadExecutor()

            every { mockWriter.write(any(), report) } answers {
                writingStarted.countDown()

                check(
                    allowWritingToFinish.await(
                        5,
                        TimeUnit.SECONDS,
                    ),
                ) {
                    "Timed out while waiting to finish report writing"
                }
            }

            try {
                val firstRequest = executor.submit {
                    sut.createReport(
                        context = mockContext,
                        report = report,
                        cooldownDuration = 0.minutes,
                    )
                }

                check(
                    writingStarted.await(
                        5,
                        TimeUnit.SECONDS,
                    ),
                ) {
                    "Report writing did not start"
                }

                sut.createReport(
                    context = mockContext,
                    report = report,
                    cooldownDuration = 0.minutes,
                )

                verify(exactly = 1) { mockWriter.write(any(), report) }

                allowWritingToFinish.countDown()
                firstRequest.get(5, TimeUnit.SECONDS)
            } finally {
                allowWritingToFinish.countDown()
                executor.shutdownNow()
            }
        }

        context("exception handling") {

            should("release in-progress state after an I/O error") {
                val report = report()
                every { mockWriter.write(any(), report) } throws IOException("Test error")

                sut.createReport(
                    context = mockContext,
                    report = report,
                    cooldownDuration = 0.minutes,
                )

                every { mockWriter.write(any(), report) } just Runs

                sut.createReport(
                    context = mockContext,
                    report = report,
                    cooldownDuration = 0.minutes,
                )

                verify(exactly = 2) { mockWriter.write(any(), report) }
            }

            should("not apply cooldown when report creation fails") {
                val report = report()
                every { mockWriter.write(any(), report) } throws IOException("Test error")

                sut.createReport(
                    context = mockContext,
                    report = report,
                    cooldownDuration = 5.minutes,
                )

                every { mockWriter.write(any(), report) } just Runs

                sut.createReport(
                    context = mockContext,
                    report = report,
                    cooldownDuration = 5.minutes,
                )

                verify(exactly = 2) { mockWriter.write(any(), report) }
            }

            should("log unsupported operation and allow retry") {
                val report = report()
                every { mockWriter.write(any(), report) } throws UnsupportedOperationException("Test error")

                sut.createReport(
                    context = mockContext,
                    report = report,
                    cooldownDuration = 0.minutes,
                )

                every { mockWriter.write(any(), report) } just Runs

                sut.createReport(
                    context = mockContext,
                    report = report,
                    cooldownDuration = 0.minutes,
                )

                verify(exactly = 2) { mockWriter.write(any(), report) }
            }

            should("log security exception and allow retry") {
                val report = report()
                every { mockWriter.write(any(), report) } throws SecurityException("Test error")

                sut.createReport(
                    context = mockContext,
                    report = report,
                    cooldownDuration = 0.minutes,
                )

                every { mockWriter.write(any(), report) } just Runs

                sut.createReport(
                    context = mockContext,
                    report = report,
                    cooldownDuration = 0.minutes,
                )

                verify(exactly = 2) { mockWriter.write(any(), report) }
            }

            should("log runtime exception and allow retry") {
                val report = report()
                every { mockWriter.write(any(), report) } throws RuntimeException("Test error")

                sut.createReport(
                    context = mockContext,
                    report = report,
                    cooldownDuration = 0.minutes,
                )

                every { mockWriter.write(any(), report) } just Runs

                sut.createReport(
                    context = mockContext,
                    report = report,
                    cooldownDuration = 0.minutes,
                )

                verify(exactly = 2) { mockWriter.write(any(), report) }
            }
        }
    }

    context("requestReportAsync") {
        should("write report asynchronously") {
            val report = report()
            val pathSlot = slot<Path>()
            every { mockWriter.write(capture(pathSlot), report) } returns Unit

            sut.requestReportAsync(mockContext, report)

            testDispatcher.scheduler.advanceUntilIdle()

            verify(exactly = 1) { mockWriter.write(any(), report) }
            pathSlot.captured.parent shouldBe tempDir
                .resolve("addon")
                .resolve("low-memory")
                .resolve("reports")
            pathSlot.captured.fileName.toString().startsWith("memory-report-") shouldBe true
            pathSlot.captured.fileName.toString().endsWith(".log") shouldBe true
        }
    }
})

private fun report(): MemoryReport {
    return MemoryReport(
        action = CriticalAction.RESTART,
        currentSnapshot = MemorySnapshot(
            createdAt = 1L,
            maxBytes = 8_192L,
            allocatedBytes = 4_096L,
            freeAllocatedBytes = 2_048L
        ),
        historySummary = RetentionSummary(
            snapshotsCount = 1,
            retentionWindow = 5.minutes,
            averageUsedBytes = 2L,
            averageAvailableBytes = 6L,
            averageUsedPercent = 25.0,
            averageAvailablePercent = 75.0,
            minAvailableBytes = 6L,
            maxUsedBytes = 2L
        ),
        memoryCountTime = 5.minutes,
        thresholdPercent = 10.0
    )
}
