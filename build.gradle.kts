import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotlinxSerialization)
    idea
}

evaluationDependsOn(":common")
evaluationDependsOn(":feature:autorestart")
evaluationDependsOn(":feature:lowtps")

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

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
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
            sourceSet(project(":common").sourceSets.main.get())
            sourceSet(project(":feature:autorestart").sourceSets.main.get())
            sourceSet(project(":feature:lowtps").sourceSets.main.get())
        }
    }

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG

//            systemProperty("forge.logging.markers", "")
//            systemProperty("forge.logging.console.level", "info")
            jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
            jvmArgument("-XX:+UnlockExperimentalVMOptions")

            systemProperty("mixin.debug.export", "true")
            systemProperty("mixin.debug.verbose", "true")

            programArgument("-mixin.config=$modId.mixins.json")
            systemProperty("mixin.env.remapRefMap", "true")
            systemProperty("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
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


//val localRuntime: Configuration by configurations.creating
//
//configurations {
//    configurations.named("runtimeClasspath") {
//        extendsFrom(localRuntime)
//    }
//}
//
//obfuscation {
//    createRemappingConfiguration(localRuntime)
//}

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
    modImplementation(projects.common)
    modImplementation(projects.feature.autorestart)
    modImplementation(projects.feature.lowtps)

    // Kotlin For Forge
    implementation(libs.kotlinforforge)

    implementation(libs.kotlinxSerialization)

    implementation(jarJar("io.github.llamalad7:mixinextras-forge:0.4.1")!!)
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${libs.versions.mixin.get()}")!!)

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
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

kotlin {
    jvmToolchain(17)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xwhen-guards"))
}

tasks.named<Jar>("jar") {
    val commonJar = project(":common").tasks.named<Jar>("jar")
    val autoRestartJar = project(":feature:autorestart").tasks.named<Jar>("jar")
    val lowTpsJar = project(":feature:lowtps").tasks.named<Jar>("jar")

    dependsOn(commonJar, autoRestartJar, lowTpsJar)

    from({ zipTree(commonJar.get().archiveFile.get().asFile) })
    from({ zipTree(autoRestartJar.get().archiveFile.get().asFile) })
    from({ zipTree(lowTpsJar.get().archiveFile.get().asFile) })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest.attributes(
        hashMapOf(
            "MixinConfigs" to "$modId.mixins.json"
        )
    )
}