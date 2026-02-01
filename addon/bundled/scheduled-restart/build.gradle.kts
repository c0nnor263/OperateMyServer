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
    implementation(projects.omsCommon)

    implementation(libs.kotlinforforge)

    testImplementation(projects.omsTesting)
}