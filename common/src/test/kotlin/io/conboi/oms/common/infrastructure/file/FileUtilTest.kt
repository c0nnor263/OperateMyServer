package io.conboi.oms.common.infrastructure.file

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

class FileUtilTest : FunSpec({

    lateinit var tempDir: Path

    beforeEach {
        tempDir = Files.createTempDirectory("file_util_test_")
    }

    afterEach {
        tempDir.toFile().deleteRecursively()
    }

    context("ensureDir") {
        test("should create directory if it does not exist") {
            val dir = tempDir.resolve("newDir")
            dir.exists() shouldBe false

            FileUtil.ensureDir(dir)

            dir.exists() shouldBe true
            Files.isDirectory(dir) shouldBe true
        }

        test("should not throw if directory already exists") {
            val dir = tempDir.resolve("existingDir")
            Files.createDirectories(dir)
            dir.exists() shouldBe true

            FileUtil.ensureDir(dir)

            dir.exists() shouldBe true
        }
    }

    context("writeSafe") {
        test("should write content to file safely") {
            val file = tempDir.resolve("testFile.txt")
            val content = "Hello, World!"

            FileUtil.writeSafe(file, content)

            file.exists() shouldBe true
            file.readText() shouldBe content
        }

        test("should overwrite existing file content") {
            val file = tempDir.resolve("testFile.txt")
            Files.writeString(file, "Old Content")
            val newContent = "New Content"

            FileUtil.writeSafe(file, newContent)

            file.exists() shouldBe true
            file.readText() shouldBe newContent
        }

        test("should create parent directories if they do not exist") {
            val file = tempDir.resolve("nested/dir/testFile.txt")
            val content = "Nested Content"
            file.parent.exists() shouldBe false

            FileUtil.writeSafe(file, content)

            file.parent.exists() shouldBe true
            file.exists() shouldBe true
            file.readText() shouldBe content
        }
    }

})