import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.benManesVersions)
    idea
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

legacyForge {
    version = libs.versions.forge.get()

    parchment {
        mappingsVersion = libs.versions.parchment.get()
        minecraftVersion = libs.versions.minecraft.get()
    }

    runs {
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

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

val localRuntime: Configuration by configurations.creating

configurations {
    configurations.named("runtimeClasspath") {
        extendsFrom(localRuntime)
    }
}

obfuscation {
    createRemappingConfiguration(localRuntime)
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        content { includeGroup("thedarkcolour") }
    }
    maven("https://maven.parchmentmc.org") // Parchment mappings
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") // Forge Config API Port
}

dependencies {
    // Kotlin For Forge
    implementation(libs.kotlinforforge)
}
tasks.processResources {
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
//        "create_version" to libs.versions.create.get(),
    )

    inputs.properties(properties)
    filesMatching("META-INF/mods.toml") {
        expand(properties)
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(17)
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xwhen-guards"))
}