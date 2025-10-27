package tasks

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class GenerateIndexHtmlTask @Inject constructor() : DefaultTask() {

    @get:Input
    abstract val title: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    override fun getGroup(): String? {
        return "documentation"
    }

    override fun getDescription(): String? {
        return "Generates index.html listing all files in the folder"
    }

    @TaskAction
    fun generate() {
        val root = outputDir.get().asFile
        if (!root.exists() || !root.isDirectory) {
            logger.warn("Directory does not exist: ${root.absolutePath}")
            return
        }

        logger.lifecycle("Generating index.html recursively from: ${root.absolutePath}")

        val directories = root.walkTopDown()
            .filter { it.isDirectory }


        directories.forEach { dir ->
            processDirectory(root, dir)
        }
    }

    fun processDirectory(root: File, dir: File) {
        val files = dir.listFiles()?.sortedBy { it.name }?.filterNot {
            it.extension in listOf("md5", "sha1", "sha256", "sha512") || it.name.endsWith(".module")
        } ?: return
        val relativePath = dir.relativeTo(root).invariantSeparatorsPath
        val titleText = if (relativePath.isEmpty()) title.get() else "Index of /$relativePath"

        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html><head><meta charset=\"utf-8\"><title>$titleText</title></head><body>")
            appendLine("<h1>$titleText</h1>")
            appendLine("<ul>")
            if (dir != root) {
                appendLine("""<li><a href="../">../</a></li>""")
            }
            files.forEach {
                val name = it.name + if (it.isDirectory) "/" else ""
                if (name != "index.html") {
                    appendLine("""<li><a href="$name">$name</a></li>""")
                }
            }
            appendLine("</ul></body></html>")
        }

        val outFile = File(dir, "index.html")
        outFile.writeText(html)

        logger.lifecycle("index.html generated at: ${outFile.relativeTo(root)}")
    }
}