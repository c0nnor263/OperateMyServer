import io.kotest.framework.gradle.KotestGradleExtension

plugins {
    idea
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.benManesVersions) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.modPublisher) apply false
}

tasks.register("generateTemplates") {
}

subprojects {
    plugins.withId("io.kotest") {
        extensions.configure<KotestGradleExtension> {
            alwaysRerunTests = true
        }
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        maxParallelForks = (java.lang.Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        forkEvery = 50
        reports.html.required = false
        reports.junitXml.required = false
    }
}

idea {
    module {
        val excludedNames = setOf(
            "build",
            "run",
            "out",
            "logs",
            ".gradle",
            ".kotlin"
        )

        excludeDirs.addAll(
            rootDir.walkTopDown()
                .onEnter { dir ->
                    dir.name !in setOf(".git", ".idea")
                }
                .filter { dir ->
                    dir.isDirectory && dir.name in excludedNames
                }
                .toSet()
        )
    }
}