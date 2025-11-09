package io.conboi.oms.content

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.core.foundation.reason.CrashStop
import io.conboi.oms.infrastructure.file.OMSPaths
import io.conboi.oms.infrastructure.file.StopEntryLog
import io.conboi.oms.utils.foundation.TimeHelper
import io.conboi.oms.utils.infrastructure.OMSJson
import io.conboi.oms.utils.infrastructure.file.FileUtil
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import java.nio.file.Files
import java.nio.file.Path
import java.time.ZonedDateTime
import net.minecraft.server.MinecraftServer

class StopManagerTest : FunSpec({

    lateinit var tempDir: Path
    lateinit var stopCausePath: Path

    val mockReason = mockk<StopReason>()
    val mockZonedTime = mockk<ZonedDateTime>(relaxed = true)

    beforeEach {
        tempDir = Files.createTempDirectory("oms_test")
        stopCausePath = tempDir.resolve("stop_cause.json")

        StopManager.clearReason()

        mockkObject(OMSJson)
        mockkObject(TimeHelper)
        mockkObject(FileUtil)
        mockkObject(OMSPaths)

        every { OMSJson.encodeToString(StopEntryLog.serializer(), any()) } returns "json"
        every { TimeHelper.currentTime } returns mockZonedTime
        every { OMSPaths.common } returns tempDir
        every { FileUtil.writeSafe(stopCausePath, any()) } just Runs

        every { mockReason.name } returns "test_reason"
        every { mockReason.messageId } returns "oms.stopping.test_reason"
    }

    afterEach {
        tempDir.toFile().deleteRecursively()
        unmockkAll()
    }

    context("isServerStopping") {
        test("should return false when no reason") {
            StopManager.isServerStopping() shouldBe false
        }

        test("should return true when reason set") {
            StopManager.writeReason(mockReason)
            StopManager.isServerStopping() shouldBe true
        }
    }

    context("writeReason") {
        test("should write correct reason to file") {
            StopManager.writeReason(mockReason)

            verify {
                OMSJson.encodeToString(StopEntryLog.serializer(), withArg {
                    it.reason shouldBe "TEST_REASON"
                    it.time shouldBe mockZonedTime.toString()
                })
            }
            verify { FileUtil.writeSafe(stopCausePath, "json") }
        }
    }

    context("installHook") {
        test("should install shutdown hook and write CrashStop if no reason") {
            mockkStatic(Runtime::class)
            val runtime = mockk<Runtime>(relaxed = true)
            val threadSlot = slot<Thread>()

            every { Runtime.getRuntime() } returns runtime
            every { runtime.addShutdownHook(capture(threadSlot)) } just Runs
            mockkObject(StopManager)
            every { StopManager.writeReason(any()) } just Runs
            every { StopManager.isServerStopping() } returns false

            StopManager.installHook()
            threadSlot.captured.run()

            verify { StopManager.writeReason(ofType<CrashStop>()) }
        }

        test("should skip write if reason is already set") {
            mockkStatic(Runtime::class)
            val runtime = mockk<Runtime>(relaxed = true)
            val threadSlot = slot<Thread>()

            every { Runtime.getRuntime() } returns runtime
            every { runtime.addShutdownHook(capture(threadSlot)) } just Runs

            StopManager.writeReason(mockReason)

            mockkObject(StopManager)
            every { StopManager.writeReason(any()) } just Runs

            StopManager.installHook()
            threadSlot.captured.run()

            verify(exactly = 0) { StopManager.writeReason(ofType<CrashStop>()) }
        }
    }

    context("stop") {
        test("should broadcast message and halt server") {
            val server = mockk<MinecraftServer>(relaxed = true)
            val event = OMSLifecycle.StopRequestedEvent(server, mockReason)

            StopManager.stop(event)

            verify { FileUtil.writeSafe(any(), any()) }
            verify {
                server.playerList.broadcastSystemMessage(match {
                    it.string.contains("oms.stopping.test_reason")
                }, false)
            }

            verify { server.halt(false) }
        }
    }
})