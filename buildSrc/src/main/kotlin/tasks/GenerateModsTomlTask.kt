package tasks

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class GenerateModsTomlTask @Inject constructor() : DefaultTask() {

    @get:Input
    abstract val modId: Property<String>

    @get:Input
    abstract val modVersion: Property<String>

    @get:Input
    abstract val displayName: Property<String>

    @get:Input
    abstract val authors: Property<String>

    @get:Input
    abstract val modDescription: Property<String>

    @get:Input
    abstract val license: Property<String>

    @get:Input
    abstract val minecraftVersion: Property<String>

    @get:Input
    abstract val forgeVersion: Property<String>

    @get:Input
    abstract val loaderVersion: Property<String>

    @get:Input
    abstract val extras: MapProperty<String, Any>


    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile
        dir.mkdirs()

        val file = File(dir, "mods.toml")

        file.writeText(
            """
            modLoader = "kotlinforforge"
            loaderVersion = "[${loaderVersion.get()},)"
            license = "${license.get()}"

            [[mods]]
            modId = "${modId.get()}"
            version = "${modVersion.get()}"
            displayName = "${displayName.get()}"
            authors = "${authors.get()}"
            description = '''${modDescription.get()}'''
            logoFile = "icon.png"

            [[dependencies.${modId.get()}]]
            modId = "forge"
            mandatory = true
            versionRange = "[${forgeVersion.get()},)"
            ordering = "NONE"
            side = "SERVER"

            [[dependencies.${modId.get()}]]
            modId = "minecraft"
            mandatory = true
            versionRange = "${minecraftVersion.get()}"
            ordering = "NONE"
            side = "SERVER"
            """.trimIndent()
        )
        val extrasMap = extras.orNull ?: emptyMap()

        if (extrasMap.isNotEmpty()) {
            file.appendText("\n")
            extrasMap.forEach { (key, value) ->
                file.appendText(
                    "$key = ${formatTomlValue(value)}\n"
                )
            }
        }
    }

    private fun formatTomlValue(v: Any): String = when (v) {
        is String -> "\"$v\""
        is Boolean -> v.toString()
        is Number -> v.toString()
        is List<*> -> v.joinToString(
            prefix = "[", postfix = "]"
        ) { formatTomlValue(it!!) }

        else -> error("Unsupported TOML extra type: ${v::class}")
    }

}
