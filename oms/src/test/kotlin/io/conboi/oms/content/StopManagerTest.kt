package io.conboi.oms.content

import io.conboi.oms.OmsAddons
import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.api.infrastructure.file.AddonPaths
import io.conboi.oms.api.infrastructure.log.AddonLogger
import io.conboi.oms.common.foundation.TimeFormatter
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.foundation.reason.CrashStop
import io.conboi.oms.common.infrastructure.OMSJson
import io.conboi.oms.common.infrastructure.file.FileUtil
import io.conboi.oms.infrastructure.config.OMSConfigs
import io.conboi.oms.infrastructure.file.StopEntryLog
import io.conboi.oms.infrastructure.log.AddonLoggerRegistry
import io.conboi.oms.oms
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
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

class StopManagerTest : ShouldSpec({

    lateinit var tempDir: Path

    val mockContext: AddonContext = mockk(relaxed = true)
    val mockServer = mockk<MinecraftServer>(relaxed = true)
    val mockReason = mockk<StopReason>()
    val mockLogger = mockk<AddonLogger>(relaxed = true)
    val mockAddonPaths = mockk<AddonPaths>(relaxed = true)

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
        mockkObject(AddonLoggerRegistry)
        mockkObject(OMSConfigs)
        mockkStatic(Runtime::class)
    }

    beforeEach {
        tempDir = Files.createTempDirectory("oms_test")

        StopManager::class.java
            .getDeclaredField("explicitStopReason")
            .apply { isAccessible = true }
            .set(StopManager, null)

        every { TimeHelper.currentTime } returns time

        every { mockAddonPaths.common } returns tempDir
        every { mockAddonPaths.logs } returns tempDir

        every { mockContext.paths } returns mockAddonPaths
        every { OmsAddons.oms.context } returns mockContext

        every { OMSJson.encodeToString(any(), any<StopReason>()) } returns "json"
        every { FileUtil.writeSafe(any(), any()) } just Runs

        every { mockReason.name } returns "test_reason"
        every { mockReason.messageId } returns "oms.stop_reason.test_reason"

        every {
            Component.translatable(mockReason.messageId).string
        } returns "Test reason message"

        every {
            OMSConfigs.server.common.stopReasonLogging.get()
        } returns false

        every {
            AddonLoggerRegistry.persistent(any(), any())
        } returns mockLogger
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

        should("write StopEntryLog to stop_cause.json") {
            val expectedTime = TimeFormatter.formatDateTime(time)

            StopManager.writeReason(mockReason)

            verify {
                OMSJson.encodeToString(
                    StopEntryLog.serializer(),
                    withArg {
                        it.reason shouldBe "TEST_REASON"
                        it.message shouldBe "Test reason message"
                        it.time shouldBe expectedTime
                    }
                )
            }

            verify {
                FileUtil.writeSafe(
                    tempDir.resolve("stop_cause.json"),
                    "json"
                )
            }
        }
    }

    context("installHook") {

        should("write CrashStop if no explicit reason") {
            val runtime = mockk<Runtime>()
            val slot = slot<Thread>()

            every { Runtime.getRuntime() } returns runtime
            every { runtime.addShutdownHook(capture(slot)) } just Runs

            StopManager.installHook()
            slot.captured.run()

            verify {
                OMSJson.encodeToString(
                    StopEntryLog.serializer(),
                    withArg {
                        it.reason shouldBe CrashStop.name.uppercase()
                    }
                )
            }
        }

        should("not overwrite explicit reason") {
            val runtime = mockk<Runtime>()
            val slot = slot<Thread>()

            every { Runtime.getRuntime() } returns runtime
            every { runtime.addShutdownHook(capture(slot)) } just Runs

            StopManager.writeReason(mockReason)
            StopManager.installHook()
            slot.captured.run()

            verify(exactly = 0) {
                OMSJson.encodeToString(
                    StopEntryLog.serializer(),
                    match { it.reason == "CRASH" }
                )
            }
        }
    }

    context("stop") {

        should("write reason, broadcast message and halt server") {
            val event = OMSActions.StopRequestedEvent(mockServer, mockReason)

            StopManager.stop(event)

            val slot = slot<Component>()

            verify {
                mockServer.playerList.broadcastSystemMessage(capture(slot), false)
            }
            slot.captured.string shouldBe "Test reason message"
            verify { mockServer.halt(false) }
            verify { FileUtil.writeSafe(any(), any()) }
        }
    }

    context("writeReason logging") {

        should("log stop reason when stopReasonLogging is enabled") {
            every {
                OMSConfigs.server.common.stopReasonLogging.get()
            } returns true

            val logger = AddonLoggerRegistry.persistent("restart", { tempDir })

            StopManager.writeReason(mockReason)

            verify {
                logger.info(
                    "Server stopping due to reason: TEST_REASON - Test reason message"
                )
            }
        }
    }

})
