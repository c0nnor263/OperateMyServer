plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kotest)
    idea
}

apply<ModDevPlugin>()

legacyForge {
    version = libs.versions.forge.get()

    parchment {
        mappingsVersion = libs.versions.parchment.get()
        minecraftVersion = libs.versions.minecraft.get()
    }

    addModdingDependenciesTo(sourceSets.test.get())
}

dependencies {
    implementation(projects.omsApi)
    implementation(projects.omsCore)
    implementation(projects.omsUtils)

    implementation(libs.kotlinforforge)

    testImplementation(projects.omsTesting)
}