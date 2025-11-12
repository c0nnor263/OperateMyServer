import tasks.MergeLangFilesTask

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kotest)
    alias(libs.plugins.modPublisher)
    idea
}
apply<ModDevPlugin>()

val modId = WatchdogEssentials.ID
val modVersion = WatchdogEssentials.VERSION
val modDisplayName = WatchdogEssentials.DISPLAY_NAME
val modAuthors = WatchdogEssentials.AUTHOR
val modDescription = WatchdogEssentials.DESCRIPTION
val modLicense = WatchdogEssentials.LICENSE
val modGroupId = WatchdogEssentials.GROUP_ID

group = modGroupId
version = "${modVersion}+mc${libs.versions.minecraft.get()}"

val dependentProjects = listOf(
    rootProject.project(":addon:watchdog-essentials:core"),
    rootProject.project(":addon:watchdog-essentials:addon:empty-server-restart"),
    rootProject.project(":addon:watchdog-essentials:addon:low-tps"),
)
val mergedLangDir = layout.buildDirectory.dir("generated/resources/assets/$modId/lang")

dependentProjects.forEach {
    evaluationDependsOn(it.path)
}

base {
    archivesName = modId
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
        }

        create("client") {
            client()
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        create("server") {
            server()
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
        implementation(it)
    }
    modImplementation(projects.oms)
    implementation(projects.omsUtils)

    implementation(libs.kotlinforforge)

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

sourceSets.main.get().resources.srcDir("src/generated/resources")

tasks.named<Jar>("jar") {
    sourceSets {
        dependentProjects.forEach {
            from(it.sourceSets.main.get().output)
        }
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val mergeLangFiles by tasks.registering(MergeLangFilesTask::class) {
    modId.set(WatchdogEssentials.ID)
    outputDir.set(mergedLangDir)
    projectPaths.set(dependentProjects.map { it.path })
}
//
//publisher {
//    apiKeys {
//        curseforge(System.getenv("CURSE_FORGE_API_KEY"))
//    }
//
//    curseID.set("1341025")
//    versionType.set("release")
//    changelog.set(file("CHANGELOG.md"))
//    version.set(project.version.toString())
//    displayName.set("$modDisplayName $modVersion")
//    setGameVersions(libs.versions.minecraft.get())
//    setLoaders(ModLoader.FORGE, ModLoader.NEOFORGE)
//    setCurseEnvironment(CurseEnvironment.SERVER)
//    artifact.set("build/libs/${base.archivesName.get()}-${project.version}.jar")
//
//    curseDepends {
//        required("kotlin-for-forge")
//    }
//}


dependentProjects.forEach { project ->
    project.tasks.matching { it.name == "processResources" }.configureEach {
        (this as ProcessResources).exclude("assets/$modId/lang/*.json")
    }
}