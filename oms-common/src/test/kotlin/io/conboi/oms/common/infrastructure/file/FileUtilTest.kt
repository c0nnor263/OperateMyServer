package io.conboi.oms.common.infrastructure.file

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class FileUtilTest : ShouldSpec({

    lateinit var tempDir: Path

    beforeEach {
        tempDir = Files.createTempDirectory("file_util_test")
    }

    afterEach {
        tempDir.toFile().deleteRecursively()
    }

    context("ensureDir") {

        should("create directory when missing") {
            val dir = tempDir.resolve("new")

            dir.exists() shouldBe false

            FileUtil.ensureDir(dir)

            dir.exists() shouldBe true
            Files.isDirectory(dir) shouldBe true
        }

        should("do nothing when directory already exists") {
            val dir = tempDir.resolve("exists")
            Files.createDirectories(dir)

            FileUtil.ensureDir(dir)

            dir.exists() shouldBe true
            Files.isDirectory(dir) shouldBe true
        }
    }

    context("writeSafe") {

        should("write content to file") {
            val file = tempDir.resolve("file.txt")

            FileUtil.writeSafe(file, "Hello")

            file.exists() shouldBe true
            file.readText() shouldBe "Hello"
        }

        should("overwrite existing file content") {
            val file = tempDir.resolve("file.txt")
            file.writeText("Old")

            FileUtil.writeSafe(file, "New")

            file.readText() shouldBe "New"
        }

        should("create parent directories when missing") {
            val file = tempDir.resolve("nested/path/file.txt")

            FileUtil.writeSafe(file, "Content")

            file.exists() shouldBe true
            file.readText() shouldBe "Content"
        }

        should("overwrite stale .tmp file safely") {
            val file = tempDir.resolve("f.txt")
            val tmp = file.resolveSibling("f.txt.tmp")

            tmp.writeText("Old tmp")

            FileUtil.writeSafe(file, "Updated")

            file.exists() shouldBe true
            file.readText() shouldBe "Updated"
        }
    }
})
