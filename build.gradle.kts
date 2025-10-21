plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.modDevGradle) apply false
    alias(libs.plugins.benManesVersions) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.modPublisher) apply false
    idea
}

tasks.register("generateTemplates") {
}

subprojects {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}