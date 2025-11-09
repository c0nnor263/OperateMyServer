package io.conboi.oms.api.infrastructure.file

import io.conboi.oms.api.infrastructure.file.OMSRootPath.ensure
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Files
import java.nio.file.Path
import net.minecraft.server.MinecraftServer

class OMSRootPathTest : FunSpec({

    lateinit var tempDir: Path

    val mockServer = mockk<MinecraftServer>()

    beforeEach {
        tempDir = Files.createTempDirectory("oms_test")

        every { mockServer.serverDirectory } returns tempDir.toFile()
    }

    afterEach {
        val field = OMSRootPath::class.java.getDeclaredField("cachedRootPath")
        field.isAccessible = true
        field.set(null, null)

        tempDir.toFile().deleteRecursively()

        clearAllMocks()
    }

    context("init") {
        test("should create oms directory and cache root") {
            OMSRootPath.init(mockServer)

            val expectedPath = tempDir.resolve("oms")
            expectedPath.toFile().exists() shouldBe true
            OMSRootPath.root shouldBe expectedPath
        }

        test("should not recreate oms directory if it already exists") {
            val existingPath = tempDir.resolve("oms")
            existingPath.toFile().mkdirs()
            existingPath.toFile().exists() shouldBe true

            OMSRootPath.init(mockServer)

            existingPath.toFile().exists() shouldBe true
            OMSRootPath.root shouldBe existingPath
        }
    }

    context("root") {
        test("should throw if not initialized") {
            shouldThrow<IllegalStateException> {
                OMSRootPath.root
            }
        }
    }

    context("ensure") {
        test("should create directory if it does not exist") {
            val dirName = "testDir"
            val ensuredPath = tempDir.ensure(dirName)

            ensuredPath.toFile().exists() shouldBe true
            ensuredPath.toFile().isDirectory shouldBe true
        }

        test("should return existing directory if it already exists") {
            val dirName = "existingDir"
            val existingPath = tempDir.resolve(dirName)
            existingPath.toFile().mkdirs()

            val ensuredPath = tempDir.ensure(dirName)

            ensuredPath shouldBe existingPath
            ensuredPath.toFile().exists() shouldBe true
            ensuredPath.toFile().isDirectory shouldBe true
        }
    }

})