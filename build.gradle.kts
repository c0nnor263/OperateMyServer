import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotlinxSerialization)
    idea
}
apply<ModDevPlugin>()

val dependentProjects = rootProject.subprojects.filter {
    it.path != project.path && (
            it.path == ":common" || it.path.startsWith(":feature:")
            )
}
val mergedLangDir = layout.buildDirectory.dir("generated/resources/assets/operatemyserver/lang")

dependentProjects.forEach {
    evaluationDependsOn(it.path)
}

val modId: String by project
val modVersion: String by project
val modDisplayName: String by project
val modAuthors: String by project
val modDescription: String by project
val modLicense: String by project
val modGroupId: String by project

group = modGroupId
version = "${modVersion}+mc${libs.versions.minecraft.get()}"

base {
    archivesName = modId
}

mixin {
    add(sourceSets.main.get(), "$modId.refmap.json")
    config("$modId.mixins.json")
}

legacyForge {
    version = libs.versions.forge.get()

    parchment {
        mappingsVersion = libs.versions.parchment.get()
        minecraftVersion = libs.versions.minecraft.get()
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
            dependentProjects.forEach {
                sourceSet(project(it.path).sourceSets.main.get())
            }
        }
    }

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG

            jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
            jvmArgument("-XX:+UnlockExperimentalVMOptions")

            systemProperty("mixin.debug.export", "true")
            systemProperty("mixin.debug.verbose", "true")

            programArgument("-mixin.config=$modId.mixins.json")
            systemProperty("mixin.env.remapRefMap", "true")
            systemProperty("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            // TODO: Test
            systemProperty("debug", "true")
        }

        create("client") {
            client()
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        create("data") {
            data()

            programArguments.addAll(
                "--mod",
                modId,
                "--all",
                "--output",
                file("src/generated/resources/").absolutePath,
                "--existing",
                file("src/main/resources/").absolutePath
            )
        }
    }
}

dependencies {
    dependentProjects.forEach {
        modImplementation(it)
    }

    // Kotlin For Forge
    implementation(libs.kotlinforforge)

    implementation(libs.kotlinxSerialization)

    implementation(jarJar("io.github.llamalad7:mixinextras-forge:0.4.1")!!)
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${libs.versions.mixin.get()}")!!)

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

tasks.processResources {
    dependsOn(mergeLangFiles)

    // assets/lang
    from(mergedLangDir) {
        into("assets/operatemyserver/lang")
    }

    // set up properties for filling into metadata
    val properties = mapOf(
        "mod_id" to modId,
        "mod_version" to modVersion,
        "mod_display_name" to modDisplayName,
        "mod_authors" to modAuthors,
        "mod_description" to modDescription,
        "mod_license" to modLicense,

        "minecraft_version" to libs.versions.minecraft.get(),
        "forge_version" to libs.versions.forge.get().split("\\.")[0],
    )

    inputs.properties(properties)
    filesMatching("META-INF/mods.toml") {
        expand(properties)
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

subprojects {
    tasks.matching { it.name == "processResources" }.configureEach {
        (this as ProcessResources).exclude("assets/operatemyserver/lang/*.json")
    }

    val generateBuildConstants by tasks.registering {
        val outputDir = layout.buildDirectory.dir("generated/buildConstants")

        outputs.dir(outputDir)

        doLast {
            val file = outputDir.get().file("$modGroupId/BuildConstants.kt").asFile
            file.parentFile.mkdirs()

            file.writeText(
                """
            package $modGroupId

            object BuildConstants {
                const val MOD_ID = "$modId"
                const val VERSION = "${project.version}"
                val DEBUG = java.lang.Boolean.getBoolean("debug")
            }
            """.trimIndent()
            )
        }
    }

    tasks.matching { it.name == "compileKotlin" }.configureEach {
        dependsOn(generateBuildConstants)
    }

    extensions.findByName("sourceSets")?.let { ext ->
        val sourceSets = ext as SourceSetContainer
        sourceSets.named("main") {
            java.srcDir(generateBuildConstants.map { it.outputs.files })
        }
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")

tasks.named<Jar>("jar") {
    dependentProjects.forEach {
        val jar = project(it.path).tasks.named<Jar>("jar")
        dependsOn(jar)
        from({ zipTree(jar.get().archiveFile.get().asFile) })
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest.attributes(
        hashMapOf(
            "MixinConfigs" to "$modId.mixins.json"
        )
    )
}

val mergeLangFiles by tasks.registering {
    group = "build"
    description = "Merge all lang JSON files from modules"

    val gson = Gson()
    val type = object : TypeToken<Map<String, String>>() {}.type

    outputs.dir(mergedLangDir)

    doLast {
        val langFileTrees = dependentProjects.map {
            project(it.path).fileTree("src/main/resources/assets/operatemyserver/lang") {
                include("*.json")
            }
        } + listOf(
            fileTree("src/main/resources/assets/operatemyserver/lang") {
                include("*.json")
            }
        )

        val allLangFiles = files(langFileTrees)

        println("📝 Found lang files:")
        allLangFiles.files.forEach { println(" - ${it.path}") }

        val localeToEntries = mutableMapOf<String, MutableMap<String, String>>()

        allLangFiles.files.forEach { file ->
            val locale = file.nameWithoutExtension
            val content = file.readText()
            val map: Map<String, String> = gson.fromJson(content, type)
            val target = localeToEntries.getOrPut(locale) { LinkedHashMap() }
            target.putAll(map)
        }

        val outputDir = mergedLangDir.get().asFile
        outputDir.mkdirs()

        for ((locale, entries) in localeToEntries) {
            val outFile = File(outputDir, "$locale.json")
            outFile.writeText(gson.toJson(entries))
        }

        println("✅ Locales merged: ${localeToEntries.keys}")
    }
}