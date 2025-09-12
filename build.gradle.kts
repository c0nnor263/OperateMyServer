import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kotest)
    idea
}
apply<ModDevPlugin>()

val modId: String by project
val modVersion: String by project
val modDisplayName: String by project
val modAuthors: String by project
val modDescription: String by project
val modLicense: String by project
val modGroupId: String by project

group = modGroupId
version = "${modVersion}+mc${libs.versions.minecraft.get()}"

val dependentProjects = rootProject.subprojects.filter {
    it.path != project.path && (
            it.path == ":common" || it.path.startsWith(":feature:")
            )
}
val mergedLangDir = layout.buildDirectory.dir("generated/resources/assets/$modId/lang")

dependentProjects.forEach {
    evaluationDependsOn(it.path)
}

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
//            programArgument("--nogui")
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

    addModdingDependenciesTo(sourceSets.test.get())
}

dependencies {
    dependentProjects.forEach {
        modImplementation(it)
    }

    implementation(libs.kotlinforforge)
    implementation(libs.kotlinxSerialization)

    implementation(jarJar(libs.mixin.extras.asProvider().get().toString())!!)
    compileOnly(annotationProcessor(libs.mixin.extras.common.get().toString())!!)
    annotationProcessor("${libs.mixin.processor.get().module}:${libs.versions.mixin.get()}:processor")

    testImplementation(libs.bundles.testing)
}

tasks.processResources {
    dependsOn(mergeLangFiles)

    // assets/lang
    from(mergedLangDir) {
        into("assets/$modId/lang")
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
//    apply<ModDevPlugin>()

    tasks.matching { it.name == "processResources" }.configureEach {
        (this as ProcessResources).exclude("assets/$modId/lang/*.json")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
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
            project(it.path).fileTree("src/main/resources/assets/$modId/lang") {
                include("*.json")
            }
        } + listOf(
            fileTree("src/main/resources/assets/$modId/lang") {
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