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
    implementation(libs.kotlinforforge)
    implementation(libs.kotlinxSerialization)

    implementation(jarJar(libs.mixin.extras.asProvider().get().toString())!!)
    compileOnly(annotationProcessor(libs.mixin.extras.common.get().toString())!!)
    annotationProcessor("${libs.mixin.processor.get().module}:${libs.versions.mixin.get()}:processor")

    testImplementation(libs.bundles.testing)
}