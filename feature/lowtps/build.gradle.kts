import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotlinxSerialization)
    idea
}

legacyForge {
    version = libs.versions.forge.get()

    parchment {
        mappingsVersion = libs.versions.parchment.get()
        minecraftVersion = libs.versions.minecraft.get()
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
    implementation(projects.common)

    // Kotlin For Forge
    implementation(libs.kotlinforforge)

    implementation(libs.kotlinxSerialization)
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