plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("net.neoforged.moddev:net.neoforged.moddev.gradle.plugin:${libs.versions.modDevGradle.get()}")
    testImplementation(libs.bundles.testing)
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    val pluginNames = listOf(
        "base",
        "forge",
        "mod"
    )
    plugins {
        pluginNames.forEach { name ->
            val capitalized = name.replaceFirstChar { it.uppercase() }
            register("moddev$capitalized") {
                id = "moddev.$name"
                implementationClass = "plugins.ModDev${capitalized}Plugin"
            }
        }
    }
}