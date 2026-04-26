plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotest)
    idea
    id("moddev.base")
    id("moddev.forge")
}

dependencies {
    modCompileOnly(libs.omsApi)
    implementation(projects.omsCommon)
}