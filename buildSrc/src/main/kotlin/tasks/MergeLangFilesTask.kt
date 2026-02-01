package tasks

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.com.google.gson.Gson
import org.jetbrains.kotlin.com.google.gson.reflect.TypeToken

abstract class MergeLangFilesTask @Inject constructor(
    private val objects: ObjectFactory
) : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val modId: Property<String>

    @get:Input
    abstract val projectPaths: ListProperty<String>
    override fun getGroup(): String? {
        return "build"
    }

    override fun getDescription(): String? {
        return "Merge all lang JSON files from modules"
    }

    @TaskAction
    fun merge() {
        val gson = Gson()
        val type = object : TypeToken<Map<String, String>>() {}.type

        val langFileTrees = projectPaths.get().map {
            project.project(it).fileTree("src/main/resources/assets/${modId.get()}/lang") {
                include("*.json")
            }
        } + listOf(
            project.fileTree("src/main/resources/assets/${modId.get()}/lang") {
                include("*.json")
            }
        )

        val allLangFiles = project.files(langFileTrees)

        println("Found lang files:")
        allLangFiles.files.forEach { println(" - ${it.path}") }

        val localeToEntries = mutableMapOf<String, MutableMap<String, String>>()

        allLangFiles.files.forEach { file ->
            val locale = file.nameWithoutExtension
            val content = file.readText()
            val map: Map<String, String> = gson.fromJson(content, type)
            val target = localeToEntries.getOrPut(locale) { LinkedHashMap() }
            target.putAll(map)
        }

        val outputDir = outputDir.get().asFile
        outputDir.mkdirs()

        for ((locale, entries) in localeToEntries) {
            val outFile = File(outputDir, "$locale.json")
            outFile.writeText(gson.toJson(entries))
        }

        println("Locales merged: ${localeToEntries.keys}")
    }
}