package tasks

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.File
import java.nio.file.Files
import org.gradle.testfixtures.ProjectBuilder

class GenerateIndexHtmlTaskTest : ShouldSpec({

    lateinit var sut: GenerateIndexHtmlTask
    lateinit var root: File

    beforeEach {
        val project = ProjectBuilder.builder().build()
        sut = project.tasks.register("generateIndex", GenerateIndexHtmlTask::class.java).get()
        root = Files.createTempDirectory("test-docs").toFile()
        sut.outputDir.set(root)
        sut.title.set("Test Index")
    }

    afterEach {
        root.deleteRecursively()
    }

    should("generate index.html for empty root directory") {
        sut.generate()

        val indexFile = File(root, "index.html")
        indexFile.shouldExist()
        indexFile.shouldBeAFile()
        indexFile.readText() shouldContain "Test Index"
    }

    should("generate index.html with files listed") {
        File(root, "foo.txt").writeText("some content")
        File(root, "bar.md5").writeText("to be ignored")
        File(root, ".DS_Store").writeText("hidden")

        sut.generate()

        val indexFile = File(root, "index.html")
        indexFile.readText().apply {
            shouldContain("foo.txt")
            shouldNotContain("bar.md5")
            shouldNotContain(".DS_Store")
        }
    }

    should("generate nested index.html for subfolders") {
        val subfolder = File(root, "nested").apply { mkdir() }
        File(subfolder, "file.txt").writeText("nested content")

        sut.generate()

        File(root, "index.html").shouldExist()
        File(subfolder, "index.html").shouldExist()

        val nestedIndex = File(subfolder, "index.html").readText()
        nestedIndex shouldContain "file.txt"
        nestedIndex shouldContain """<a href="../">../</a>"""
    }

    should("do nothing if directory does not exist") {
        val fake = File(root, "nonexistent")
        sut.outputDir.set(fake)

        // Should not throw
        sut.generate()
    }
})
