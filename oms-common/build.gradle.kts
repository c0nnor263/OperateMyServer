plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotest)
    idea
    id("moddev.base")
    id("moddev.forge")
}

modDev {
    includeTest.set(true)
}

dependencies {
    implementation(projects.omsApi)
    implementation(libs.kotlinxSerialization)

    testImplementation(projects.omsTesting)
}