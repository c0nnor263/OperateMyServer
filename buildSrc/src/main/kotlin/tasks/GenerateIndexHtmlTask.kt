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

    private fun processDirectory(root: File, dir: File) {
        val files = processFiles(dir) ?: return
        val relativePath = dir.relativeTo(root).invariantSeparatorsPath
        val titleHtml = generateTitle(relativePath)

        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html><head><meta charset=\"utf-8\"><title>Index</title>")
            appendLine(
                """
    <style>
        body {
            font-family: sans-serif;
            padding: 1em;
        }
        ul {
            list-style-type: none;
            padding-left: 0;
            margin-top: 1em;
        }
        li {
            margin: 0.25em 0;
        }
        a {
            text-decoration: none;
            color: #1565c0;
        }
        a:hover {
            text-decoration: underline;
        }
        h1 {
            font-size: 1.4em;
        }
        h1 a {
            font-weight: normal;
        }
    </style>
""".trimIndent()
            )
            appendLine("</head><body>")
            appendLine("<h1>$titleHtml</h1>")
            appendLine("<ul>")
            if (dir != root) {
                appendLine("""<li><a href="../">../</a></li>""")
            }
            files.forEach {
                val name = it.name + if (it.isDirectory) "/" else ""
                appendLine("""<li><a href="$name">$name</a></li>""")
            }
            appendLine("</ul></body></html>")
        }

        File(dir, "index.html").writeText(html)
        logger.lifecycle("index.html generated at: ${dir.relativeTo(root)}")
    }

    private fun processFiles(dir: File): List<File>? {
        return dir.listFiles()?.sortedBy { it.name }?.filterNot {
            it.extension in listOf("md5", "sha1", "sha256", "sha512") ||
                    it.name.endsWith(".module") ||
                    it.name == "index.html" ||
                    it.name.startsWith(".")
        }
    }

    private fun generateTitle(relativePath: String): String {
        if (relativePath.isEmpty()) {
            return """<a href="./">${title.get()}</a>"""
        }

        val parts = relativePath.split("/").filter { it.isNotEmpty() }
        val links = mutableListOf<String>()

        parts.forEachIndexed { index, part ->
            val href = "../".repeat(parts.size - index - 1)
            links.add("""<a href="$href">$part</a>""")
        }

        return "Index of / " + links.joinToString(" / ")
    }
}