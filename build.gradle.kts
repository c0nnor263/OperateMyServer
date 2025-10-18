plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.modDevGradle) apply false
    alias(libs.plugins.benManesVersions) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.modPublisher) apply false
    idea
}

val modId: String by project

tasks.register("generateTemplates") {
}

subprojects {
    tasks.matching { it.name == "processResources" }.configureEach {
        (this as ProcessResources).exclude("assets/$modId/lang/*.json")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}