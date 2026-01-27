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
    implementation("io.conboi.oms:oms-api:1.0.0")
    implementation(projects.omsCommon)
    implementation(projects.addon.watchdogEssentials.common)

    implementation(libs.kotlinforforge)

    testImplementation(projects.omsTesting)
}