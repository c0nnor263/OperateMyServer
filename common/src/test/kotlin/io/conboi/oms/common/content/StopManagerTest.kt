package io.conboi.oms.common.content

import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.common.foundation.reason.StopReason
import io.conboi.oms.common.infrastructure.OMSJson
import io.conboi.oms.common.infrastructure.file.FileUtil
import io.conboi.oms.common.infrastructure.file.OMSPaths
import io.conboi.oms.common.infrastructure.file.StopEntry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.verify
import net.minecraft.server.MinecraftServer

class StopManagerTest : FunSpec({

    afterEach {
        clearAllMocks()
    }

    context("isServerStopping") {
        test("should return false when no explicit reason") {
            StopManager.isServerStopping() shouldBe false
        }

        test("should return true when explicit reason is set") {
            val mockTime = "2024-01-01T00:00:00Z"
            val reason = mockk<StopReason> {
                every { name } returns "TestReason"
            }
            val mockPath
            mockkObject(TimeHelper)
            every { TimeHelper.currentTime } returns mockk {
                every { toString() } returns mockTime
            }
            mockkObject(OMSJson)
            mockkObject(FileUtil)
            mockkObject(OMSPaths)
            val entry = StopEntry("TESTREASON", mockTime)
            every { OMSJson.encodeToString(StopEntry.serializer(), entry) } returns "json"
            every { OMSPaths.stopCause() } returns "path"
            every { FileUtil.writeSafe("path", "json") } just Runs

            StopManager.writeReason(reason)

            verify { FileUtil.writeSafe("path", "json") }
            unmockkObject(TimeHelper, OMSJson, FileUtil, OMSPaths)



            val reason = mockk<StopReason>()
            StopManager.writeReason(reason)
            StopManager.isServerStopping() shouldBe true
        }
    }

    context("installHook") {
        test("should add shutdown hook") {
            mockkStatic(Runtime::class)
            val runtime = mockk<Runtime>(relaxed = true)
            every { Runtime.getRuntime() } returns runtime
            every { runtime.addShutdownHook(any()) } just Runs

            StopManager.installHook()

            verify { runtime.addShutdownHook(any()) }
        }
    }

    context("stop") {
        test("should broadcast message and halt server") {
            val server = mockk<MinecraftServer>(relaxed = true)
            val reason = mockk<StopReason> {
                every { messageId } returns "stop.message"
            }
            mockkObject(StopManager)
            every { StopManager.writeReason(reason) } just Runs

            StopManager.stop(server, reason)

            verify { StopManager.writeReason(reason) }
            verify { server.playerList.broadcastSystemMessage(any(), false) }
            verify { server.halt(false) }
        }
    }

    context("writeReason") {
        test("should write stop reason to file") {

        }
    }
})
