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
    modCompileOnly(libs.omsApi)
    implementation(projects.omsCommon)
    implementation(projects.addon.watchdogEssentials.common)

    implementation(libs.kotlinforforge)

    testImplementation(libs.omsApi)
    testImplementation(projects.omsTesting)
}