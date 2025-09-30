package io.conboi.oms.common.infrastructure.file

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Files
import java.nio.file.Path
import net.minecraft.server.MinecraftServer

class OMSPathsTest : FunSpec({

    lateinit var tempDir: Path

    val mockServer = mockk<MinecraftServer>()

    beforeEach {
        tempDir = Files.createTempDirectory("oms_test")

        every { mockServer.serverDirectory } returns tempDir.toFile()
    }

    afterEach {
        val field = OMSPaths::class.java.getDeclaredField("cachedRootPath")
        field.isAccessible = true
        field.set(null, null)

        tempDir.toFile().deleteRecursively()

        clearAllMocks()
    }

    context("init") {
        test("should create oms directory and cache root") {
            OMSPaths.init(mockServer)

            val expectedPath = tempDir.resolve("oms")
            expectedPath.toFile().exists() shouldBe true
            OMSPaths.root() shouldBe expectedPath
        }

        test("should not recreate oms directory if it already exists") {
            val existingPath = tempDir.resolve("oms")
            existingPath.toFile().mkdirs()
            existingPath.toFile().exists() shouldBe true

            OMSPaths.init(mockServer)

            existingPath.toFile().exists() shouldBe true
            OMSPaths.root() shouldBe existingPath
        }
    }

    context("stopCause") {
        test("should resolve stop_cause json") {
            OMSPaths.init(mockServer)

            val stopCausePath = OMSPaths.stopCause()
            val expectedStopCausePath = tempDir.resolve("oms").resolve("stop_cause.json")
            stopCausePath shouldBe expectedStopCausePath
        }
    }

    context("root") {
        test("should throw if not initialized") {
            shouldThrow<IllegalStateException> {
                OMSPaths.root()
            }
        }
    }

})