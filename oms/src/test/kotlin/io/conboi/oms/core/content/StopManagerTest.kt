package io.conboi.oms.core.content

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.content.StopManager
import io.conboi.oms.core.foundation.reason.CrashStop
import io.conboi.oms.core.infrastructure.file.OMSPaths
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
import io.mockk.unmockkObject
import io.mockk.verify
import java.nio.file.Files
import java.nio.file.Path
import java.time.ZonedDateTime
import net.minecraft.server.MinecraftServer

class StopManagerTest : FunSpec({

    lateinit var tempDir: Path
    lateinit var stopCausePath: Path

    val mockReason = mockk<StopReason>()
    val mockZonedDateTime = mockk<ZonedDateTime>(relaxed = true)


    beforeEach {
        tempDir = Files.createTempDirectory("oms_test")
        stopCausePath = tempDir.toFile().toPath().resolve("oms").resolve("stop_cause.json")

        StopManager.clearReason()

        mockkObject(OMSJson)
        mockkObject(TimeHelper)
        mockkObject(OMSPaths)
        mockkObject(FileUtil)
        every { OMSJson.encodeToString(StopEntryLog.serializer(), any()) } returns "json"
        every { TimeHelper.currentTime } returns mockZonedDateTime

        every { OMSPaths.stopCause() } returns stopCausePath
        every { FileUtil.writeSafe(stopCausePath, any()) } just Runs
        every { mockReason.name } returns "test_reason"
        every { mockReason.messageId } returns "oms.stopping.test_reason"
    }

    afterEach {
        tempDir.toFile().deleteRecursively()
        unmockkObject(StopManager, OMSJson, TimeHelper, OMSPaths, FileUtil)
    }

    context("isServerStopping") {
        test("should return false when no explicit reason") {
            StopManager.isServerStopping() shouldBe false
        }

        test("should return true when explicit reason is set") {
            StopManager.writeReason(mockReason)
            StopManager.isServerStopping() shouldBe true
        }
    }

    context("installHook") {
        test("should add shutdown hook") {
            mockkStatic(Runtime::class)
            val runtime = mockk<Runtime>(relaxed = true)
            val slot = slot<Thread>()

            every { Runtime.getRuntime() } returns runtime
            every { runtime.addShutdownHook(capture(slot)) } just Runs

            StopManager.installHook()

            slot.captured.name shouldBe StopManager.HOOK_NAME
            verify { runtime.addShutdownHook(slot.captured) }
        }

        @Suppress("CallToThreadRun")
        test("should write CrashStop when no explicit reason") {
            mockkStatic(Runtime::class)
            val runtime = mockk<Runtime>(relaxed = true)
            val slot = slot<Thread>()

            every { Runtime.getRuntime() } returns runtime
            every { runtime.addShutdownHook(capture(slot)) } just Runs
            mockkObject(StopManager)
            every { StopManager.writeReason(any()) } just Runs
            StopManager.isServerStopping() shouldBe false

            StopManager.installHook()

            slot.captured.run()

            verify { StopManager.writeReason(ofType<CrashStop>()) }
        }

        @Suppress("CallToThreadRun")
        test("should not write CrashStop when explicit reason is present") {
            mockkStatic(Runtime::class)
            val runtime = mockk<Runtime>(relaxed = true)
            val slot = slot<Thread>()
            every { Runtime.getRuntime() } returns runtime
            every { runtime.addShutdownHook(capture(slot)) } just Runs

            mockkObject(StopManager)
            StopManager.writeReason(mockReason)
            every { StopManager.writeReason(any()) } just Runs
            StopManager.isServerStopping() shouldBe true

            StopManager.installHook()

            slot.captured.run()

            verify(inverse = true) { StopManager.writeReason(ofType<CrashStop>()) }
        }
    }

    context("stop") {
        test("should broadcast message and halt server") {
            val server = mockk<MinecraftServer>(relaxed = true)
            mockkObject(StopManager)
            every { StopManager.writeReason(mockReason) } just Runs
            val event = OMSLifecycle.StopRequestedEvent(server, mockReason)

            StopManager.stop(event)

            verify { StopManager.writeReason(mockReason) }
            verify {
                server.playerList.broadcastSystemMessage(
                    match { it.string.contains("oms.stopping.test_reason") },
                    false
                )
            }
            verify { server.halt(false) }
        }
    }

    context("writeReason") {
        test("should write stop reason to file") {
            StopManager.writeReason(mockReason)

            val expectedName = mockReason.name.uppercase()
            val expectedTime = mockZonedDateTime.toString()
            verify {
                OMSJson.encodeToString(StopEntryLog.serializer(), withArg {
                    it.reason shouldBe expectedName
                    it.time shouldBe expectedTime
                })
            }
            verify { FileUtil.writeSafe(stopCausePath, any()) }
        }
    }
})
