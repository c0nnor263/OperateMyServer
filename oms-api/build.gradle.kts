plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotest)
    idea
    `maven-publish`
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
    testImplementation(libs.bundles.testing)
}

java {
    withSourcesJar()
}

publishing {

}