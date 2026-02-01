package io.conboi.oms.infrastructure.file

import io.conboi.oms.api.extension.ensure
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Files
import java.nio.file.Path
import net.minecraft.server.MinecraftServer

class OMSRootPathTest : ShouldSpec({

    lateinit var tempDir: Path
    val mockServer = mockk<MinecraftServer>()

    fun resetCachedRoot() {
        val field = OMSRootPath::class.java.getDeclaredField("cachedRootPath")
        field.isAccessible = true
        field.set(null, null)
    }

    beforeEach {
        tempDir = Files.createTempDirectory("oms_test")
        every { mockServer.serverDirectory } returns tempDir.toFile()
    }

    afterEach {
        resetCachedRoot()
        tempDir.toFile().deleteRecursively()
        clearAllMocks()
    }

    context("init") {

        should("create oms directory and caches root") {
            OMSRootPath.init(mockServer)

            val expected = tempDir.resolve("oms")

            expected.toFile().exists() shouldBe true
            OMSRootPath.root shouldBe expected
        }

        should("do not recreate directory if already present") {
            val existing = tempDir.resolve("oms")
            existing.toFile().mkdirs()

            OMSRootPath.init(mockServer)

            existing.toFile().exists() shouldBe true
            OMSRootPath.root shouldBe existing
        }
    }

    context("root") {

        should("throw if accessed before init") {
            val err = shouldThrow<IllegalStateException> {
                OMSRootPath.root
            }

            err.message shouldContain "not initialized"
        }
    }

    context("ensure") {

        should("create missing directory") {
            val ensured = tempDir.ensure("testDir")

            ensured.toFile().exists() shouldBe true
            ensured.toFile().isDirectory shouldBe true
        }

        should("return existing directory") {
            val existing = tempDir.resolve("existingDir")
            existing.toFile().mkdirs()

            val ensured = tempDir.ensure("existingDir")

            ensured shouldBe existing
            ensured.toFile().exists() shouldBe true
        }
    }
})
