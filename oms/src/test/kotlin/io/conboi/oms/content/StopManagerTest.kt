package io.conboi.oms.content

import io.conboi.oms.OperateMyServerAddon
import io.conboi.oms.api.OmsAddons
import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.infrastructure.file.StopEntryLog
import io.conboi.oms.oms
import io.conboi.oms.utils.foundation.TimeFormatter
import io.conboi.oms.utils.foundation.TimeHelper
import io.conboi.oms.utils.infrastructure.OMSJson
import io.conboi.oms.utils.infrastructure.file.FileUtil
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import java.nio.file.Files
import java.nio.file.Path
import java.time.ZoneId
import java.time.ZonedDateTime
import net.minecraft.server.MinecraftServer

class StopManagerTest : ShouldSpec({

    lateinit var tempDir: Path
    lateinit var stopCausePath: Path

    val mockServer = mockk<MinecraftServer>(relaxed = true)
    val mockReason = mockk<StopReason>()
    val mockAddon = mockk<OperateMyServerAddon>(relaxed = true)
    val mockRuntime = mockk<Runtime>(relaxed = true)

    val zone = ZoneId.of("UTC")
    val time = ZonedDateTime.of(
        2025, 10, 31,
        10, 0, 0, 0,
        zone
    ).toEpochSecond()

    beforeSpec {
        mockkObject(OMSJson)
        mockkObject(FileUtil)
        mockkObject(TimeHelper)
        mockkObject(OmsAddons)
        mockkStatic(Runtime::class)
    }

    beforeEach {
        tempDir = Files.createTempDirectory("oms_test")
        stopCausePath = tempDir.resolve("stop_cause.json")

        StopManager.clearReason()

        every { TimeHelper.zoneId } returns zone
        every { TimeHelper.currentTime } returns time

        every { mockAddon.paths.common } returns tempDir
        every { OmsAddons.get(any()) } returns mockAddon
        every { OmsAddons.oms } returns mockAddon

        every { OMSJson.encodeToString(StopEntryLog.serializer(), any()) } returns "json"
        every { FileUtil.writeSafe(stopCausePath, any()) } just Runs

        every { mockReason.name } returns "test_reason"
        every { mockReason.messageId } returns "oms.stop_reason.test_reason"

        every { Runtime.getRuntime() } returns mockRuntime
    }

    afterEach {
        tempDir.toFile().deleteRecursively()
        clearAllMocks()
    }

    context("isServerStopping") {
        should("return false when no reason set") {
            StopManager.isServerStopping().shouldBeFalse()
        }

        should("return true when reason is set") {
            StopManager.writeReason(mockReason)
            StopManager.isServerStopping().shouldBeTrue()
        }
    }

    context("writeReason") {
        should("write proper StopEntryLog to file") {
            val expectedTimeString = TimeFormatter.formatDateTime(time)

            StopManager.writeReason(mockReason)

            verify {
                OMSJson.encodeToString(
                    StopEntryLog.serializer(),
                    withArg {
                        it.reason shouldBe "TEST_REASON"
                        it.time shouldBe expectedTimeString
                        it.message shouldBe "oms.stop_reason.test_reason"
                    }
                )
            }

            verify {
                FileUtil.writeSafe(stopCausePath, "json")
            }
        }
    }

    context("installHook") {

        should("write CrashStop when no reason set") {
            val expectedTimeString = TimeFormatter.formatDateTime(time)
            val captured = slot<Thread>()
            every { mockRuntime.addShutdownHook(capture(captured)) } just Runs
            StopManager.installHook()
            captured.captured.run()

            verify {
                OMSJson.encodeToString(
                    StopEntryLog.serializer(),
                    withArg {
                        it.reason shouldBe "CRASH"
                        it.time shouldBe expectedTimeString
                        it.message shouldBe "oms.stop_reason.crash"
                    }
                )
            }
        }

        should("not write CrashStop if reason already set") {
            val expectedTimeString = TimeFormatter.formatDateTime(time)
            val captured = slot<Thread>()
            every { mockRuntime.addShutdownHook(capture(captured)) } just Runs

            StopManager.writeReason(mockReason)
            StopManager.installHook()
            captured.captured.run()

            verify(exactly = 0) {
                OMSJson.encodeToString(
                    StopEntryLog.serializer(),
                    withArg {
                        it.reason shouldBe "CRASH"
                        it.time shouldBe expectedTimeString
                        it.message shouldBe "oms.stop_reason.crash"
                    }
                )
            }
        }
    }

    context("stop") {
        should("save reason and halt server") {
            val event = OMSActions.StopRequestedEvent(mockServer, mockReason)

            StopManager.stop(event)

            verify { FileUtil.writeSafe(any(), any()) }
            verify { mockServer.halt(false) }
        }
    }
})