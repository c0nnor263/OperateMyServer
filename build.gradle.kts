plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.benManesVersions) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.modPublisher) apply false
}

tasks.register("generateTemplates") {
}

subprojects {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        forkEvery = 50
        reports.html.required = false
        reports.junitXml.required = false
    }
}