package tasks

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.File
import org.gradle.testfixtures.ProjectBuilder

class GenerateIndexHtmlTaskTest : FunSpec({

    lateinit var task: GenerateIndexHtmlTask
    lateinit var root: File

    beforeTest {
        val project = ProjectBuilder.builder().build()
        task = project.tasks.create("generateIndex", GenerateIndexHtmlTask::class.java)
        root = createTempDir(prefix = "test-docs")
        task.outputDir.set(root)
        task.title.set("Test Index")
    }

    afterTest {
        root.deleteRecursively()
    }

    test("should generate index.html for empty root directory") {
        task.generate()

        val indexFile = File(root, "index.html")
        indexFile.shouldExist()
        indexFile.shouldBeAFile()
        indexFile.readText() shouldContain "Test Index"
    }

    test("should generate index.html with files listed") {
        File(root, "foo.txt").writeText("some content")
        File(root, "bar.md5").writeText("to be ignored")
        File(root, ".DS_Store").writeText("hidden")

        task.generate()

        val indexFile = File(root, "index.html")
        indexFile.readText().apply {
            shouldContain("foo.txt")
            shouldNotContain("bar.md5")
            shouldNotContain(".DS_Store")
        }
    }

    test("should generate nested index.html for subfolders") {
        val subfolder = File(root, "nested").apply { mkdir() }
        File(subfolder, "file.txt").writeText("nested content")

        task.generate()

        File(root, "index.html").shouldExist()
        File(subfolder, "index.html").shouldExist()

        val nestedIndex = File(subfolder, "index.html").readText()
        nestedIndex shouldContain "file.txt"
        nestedIndex shouldContain """<a href="../">../</a>"""
    }

    test("should do nothing if directory does not exist") {
        val fake = File(root, "nonexistent")
        task.outputDir.set(fake)

        // Should not throw
        task.generate()
    }
})
